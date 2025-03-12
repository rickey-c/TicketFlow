package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.enums.BaseCode;
import com.damai.enums.BusinessStatus;
import com.damai.enums.CompositeCheckType;
import com.damai.enums.SellStatus;
import com.damai.exception.DaMaiFrameException;
import com.damai.threadlocal.BaseParameterHolder;
import com.damai.util.DateUtils;
import com.damai.util.StringUtil;
import com.ticketflow.BusinessThreadPool;
import com.ticketflow.RedisStreamPushHandler;
import com.ticketflow.client.BaseDataClient;
import com.ticketflow.client.OrderClient;
import com.ticketflow.client.UserClient;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.*;
import com.ticketflow.entity.*;
import com.ticketflow.impl.composite.CompositeContainer;
import com.ticketflow.mapper.*;
import com.ticketflow.page.PageUtil;
import com.ticketflow.page.PageVo;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketflow.service.cache.local.*;
import com.ticketflow.service.constant.ProgramTimeType;
import com.ticketflow.service.es.ProgramEs;
import com.ticketflow.service.lua.ProgramDelCacheData;
import com.ticketflow.service.tool.TokenExpireManager;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.util.ServiceLockTool;
import com.ticketflow.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.damai.constant.Constant.CODE;
import static com.damai.constant.Constant.USER_ID;
import static com.damai.util.DateUtils.FORMAT_DATE;
import static com.ticketflow.core.DistributedLockConstants.*;
import static com.ticketflow.core.RepeatExecuteLimitConstants.CANCEL_PROGRAM_ORDER;

/**
 * @Description: 节目服务
 * @Author: rickey-c
 * @Date: 2025/2/8 10:51
 */
@Slf4j
@Service
public class ProgramService extends ServiceImpl<ProgramMapper, Program> {
    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private ProgramGroupMapper programGroupMapper;

    @Autowired
    private ProgramShowTimeMapper programShowTimeMapper;

    @Autowired
    private ProgramCategoryMapper programCategoryMapper;

    @Autowired
    private TicketCategoryMapper ticketCategoryMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private BaseDataClient baseDataClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private RedisCache redisCache;

    @Lazy
    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramShowTimeService programShowTimeService;

    @Autowired
    private TicketCategoryService ticketCategoryService;

    @Autowired
    private ProgramCategoryService programCategoryService;

    @Autowired
    private ProgramEs programEs;

    @Autowired
    private ServiceLockTool serviceLockTool;

    @Autowired
    private RedisStreamPushHandler redisStreamPushHandler;

    @Autowired
    private LocalCacheProgram localCacheProgram;

    @Autowired
    private LocalCacheProgramGroup localCacheProgramGroup;

    @Autowired
    private LocalCacheProgramCategory localCacheProgramCategory;

    @Autowired
    private LocalCacheProgramShowTime localCacheProgramShowTime;

    @Autowired
    private LocalCacheTicketCategory localCacheTicketCategory;

    @Autowired
    private CompositeContainer compositeContainer;

    @Autowired
    private TokenExpireManager tokenExpireManager;

    @Autowired
    private ProgramDelCacheData programDelCacheData;

    /**
     * 添加节目
     *
     * @param programAddDto 节目添加dto
     * @return 节目id
     */
    public Long add(ProgramAddDto programAddDto) {
        Program program = new Program();
        BeanUtil.copyProperties(programAddDto, program);
        program.setId(uidGenerator.getUid());
        programMapper.insert(program);
        return program.getId();
    }

    /**
     * 搜索节目
     *
     * @param programSearchDto 搜索dto
     * @return 节目列表vo
     */
    public PageVo<ProgramListVo> search(ProgramSearchDto programSearchDto) {
        //将入参的参数进行具体的组装
        setQueryTime(programSearchDto);
        return programEs.search(programSearchDto);
    }

    /**
     * 显示主页节目
     *
     * @param programListDto 节目列表dto（区域、父节目类型）
     * @return 主页节目list
     */
    public List<ProgramHomeVo> selectHomeList(ProgramListDto programListDto) {

        List<ProgramHomeVo> programHomeVoList = programEs.selectHomeList(programListDto);
        if (CollectionUtil.isNotEmpty(programHomeVoList)) {
            return programHomeVoList;
        }
        return dbSelectHomeList(programListDto);
    }

    /**
     * 从数据库查询主页节目列表（按分类分组）
     *
     * @param programPageListDto 分页搜索参数（区域、父分类ID等）
     * @return 按分类分组的主页节目数据
     */
    private List<ProgramHomeVo> dbSelectHomeList(ProgramListDto programPageListDto) {
        // 初始化返回结构
        List<ProgramHomeVo> programHomeVoList = new ArrayList<>();

        // 1. 获取节目分类名称映射（父分类ID -> 分类名称）
        Map<Long, String> programCategoryMap = selectProgramCategoryMap(
                programPageListDto.getParentProgramCategoryIds());

        // 2. 查询基础节目列表
        List<Program> programList = programMapper.selectHomeList(programPageListDto);
        if (CollectionUtil.isEmpty(programList)) {
            return programHomeVoList; // 无数据直接返回空
        }

        // 3. 批量查询演出时间（减少数据库交互次数）
        List<Long> programIdList = programList.stream()
                .map(Program::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ProgramShowTime> showTimeQuery = Wrappers.lambdaQuery(ProgramShowTime.class)
                .in(ProgramShowTime::getProgramId, programIdList); // WHERE program_id IN (...)

        List<ProgramShowTime> programShowTimeList = programShowTimeMapper.selectList(showTimeQuery);
        // 按节目ID分组，便于后续快速查找
        Map<Long, List<ProgramShowTime>> programShowTimeMap = programShowTimeList.stream()
                .collect(Collectors.groupingBy(ProgramShowTime::getProgramId));

        // 4. 批量查询票务价格范围（最高价、最低价）
        Map<Long, TicketCategoryAggregate> ticketCategorieMap = selectTicketCategorieMap(programIdList);

        // 5. 按父分类ID分组节目
        Map<Long, List<Program>> programMap = programList.stream()
                .collect(Collectors.groupingBy(Program::getParentProgramCategoryId));

        // 6. 遍历每个分类，构建最终响应结构
        for (Map.Entry<Long, List<Program>> entry : programMap.entrySet()) {
            Long categoryId = entry.getKey();
            List<Program> categoryPrograms = entry.getValue();

            List<ProgramListVo> programVoList = new ArrayList<>();
            for (Program program : categoryPrograms) {
                ProgramListVo programVo = new ProgramListVo();
                BeanUtil.copyProperties(program, programVo); // 复制基础属性

                // 设置演出时间（取第一个时间数据）
                List<ProgramShowTime> showTimes = programShowTimeMap.get(program.getId());
                if (CollectionUtil.isNotEmpty(showTimes)) {
                    ProgramShowTime firstShowTime = showTimes.get(0); // 假设取首条记录
                    programVo.setShowTime(firstShowTime.getShowTime());
                    programVo.setShowDayTime(firstShowTime.getShowDayTime());
                    programVo.setShowWeekTime(firstShowTime.getShowWeekTime());
                }

                // 设置票务价格
                TicketCategoryAggregate ticketAggregate = ticketCategorieMap.get(program.getId());
                if (ticketAggregate != null) {
                    programVo.setMaxPrice(ticketAggregate.getMaxPrice());
                    programVo.setMinPrice(ticketAggregate.getMinPrice());
                }

                programVoList.add(programVo);
            }

            // 构建分类维度的主页对象
            ProgramHomeVo homeVo = new ProgramHomeVo();
            homeVo.setCategoryId(categoryId);
            homeVo.setCategoryName(programCategoryMap.get(categoryId)); // 分类名称
            homeVo.setProgramListVoList(programVoList);
            programHomeVoList.add(homeVo);
        }

        return programHomeVoList;
    }

    public Map<Long, String> selectProgramCategoryMap(Collection<Long> programCategoryIdList) {
        LambdaQueryWrapper<ProgramCategory> pcLambdaQueryWrapper = Wrappers.lambdaQuery(ProgramCategory.class)
                .in(ProgramCategory::getId, programCategoryIdList);
        List<ProgramCategory> programCategoryList = programCategoryMapper.selectList(pcLambdaQueryWrapper);
        return programCategoryList
                .stream()
                .collect(Collectors.toMap(ProgramCategory::getId, ProgramCategory::getName, (v1, v2) -> v2));
    }

    /**
     * 推荐列表
     *
     * @param programRecommendListDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public List<ProgramListVo> recommendList(ProgramRecommendListDto programRecommendListDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_RECOMMEND_CHECK.getValue(), programRecommendListDto);
        return programEs.recommendList(programRecommendListDto);
    }


    public void setQueryTime(ProgramPageListDto programPageListDto) {
        switch (programPageListDto.getTimeType()) {
            case ProgramTimeType.TODAY:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.now(FORMAT_DATE));
                break;
            case ProgramTimeType.TOMORROW:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.addDay(DateUtils.now(FORMAT_DATE), 1));
                break;
            case ProgramTimeType.WEEK:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.addWeek(DateUtils.now(FORMAT_DATE), 1));
                break;
            case ProgramTimeType.MONTH:
                programPageListDto.setStartDateTime(DateUtils.now(FORMAT_DATE));
                programPageListDto.setEndDateTime(DateUtils.addMonth(DateUtils.now(FORMAT_DATE), 1));
                break;
            case ProgramTimeType.CALENDAR:
                if (Objects.isNull(programPageListDto.getStartDateTime())) {
                    throw new DaMaiFrameException(BaseCode.START_DATE_TIME_NOT_EXIST);
                }
                if (Objects.isNull(programPageListDto.getEndDateTime())) {
                    throw new DaMaiFrameException(BaseCode.END_DATE_TIME_NOT_EXIST);
                }
                break;
            default:
                programPageListDto.setStartDateTime(null);
                programPageListDto.setEndDateTime(null);
                break;
        }
    }

    /**
     * 查询分类列表（数据库查询）
     *
     * @param programPageListDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public PageVo<ProgramListVo> selectPage(ProgramPageListDto programPageListDto) {
        setQueryTime(programPageListDto);
        PageVo<ProgramListVo> pageVo = programEs.selectPage(programPageListDto);
        if (CollectionUtil.isNotEmpty(pageVo.getList())) {
            return pageVo;
        }
        return dbSelectPage(programPageListDto);
    }

    /**
     * 查询分类信息（数据库查询）
     *
     * @param programPageListDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public PageVo<ProgramListVo> dbSelectPage(ProgramPageListDto programPageListDto) {
        IPage<ProgramJoinShowTime> iPage =
                programMapper.selectPage(PageUtil.getPageParams(programPageListDto), programPageListDto);
        if (CollectionUtil.isEmpty(iPage.getRecords())) {
            return new PageVo<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), new ArrayList<>());
        }
        Set<Long> programCategoryIdList =
                iPage.getRecords().stream().map(Program::getProgramCategoryId).collect(Collectors.toSet());
        Map<Long, String> programCategoryMap = selectProgramCategoryMap(programCategoryIdList);

        List<Long> programIdList = iPage.getRecords().stream().map(Program::getId).collect(Collectors.toList());
        Map<Long, TicketCategoryAggregate> ticketCategorieMap = selectTicketCategorieMap(programIdList);

        Map<Long, String> tempAreaMap = new HashMap<>(64);
        AreaSelectDto areaSelectDto = new AreaSelectDto();
        areaSelectDto.setIdList(iPage.getRecords().stream().map(Program::getAreaId).distinct().collect(Collectors.toList()));
        ApiResponse<List<AreaVo>> areaResponse = baseDataClient.selectByIdList(areaSelectDto);
        if (Objects.equals(areaResponse.getCode(), ApiResponse.ok().getCode())) {
            if (CollectionUtil.isNotEmpty(areaResponse.getData())) {
                tempAreaMap = areaResponse.getData().stream()
                        .collect(Collectors.toMap(AreaVo::getId, AreaVo::getName, (v1, v2) -> v2));
            }
        } else {
            log.error("base-data selectByIdList rpc error areaResponse:{}", JSON.toJSONString(areaResponse));
        }
        Map<Long, String> areaMap = tempAreaMap;

        return PageUtil.convertPage(iPage, programJoinShowTime -> {
            ProgramListVo programListVo = new ProgramListVo();
            BeanUtil.copyProperties(programJoinShowTime, programListVo);

            programListVo.setAreaName(areaMap.get(programJoinShowTime.getAreaId()));
            programListVo.setProgramCategoryName(programCategoryMap.get(programJoinShowTime.getProgramCategoryId()));
            programListVo.setMinPrice(Optional.ofNullable(ticketCategorieMap.get(programJoinShowTime.getId()))
                    .map(TicketCategoryAggregate::getMinPrice).orElse(null));
            programListVo.setMaxPrice(Optional.ofNullable(ticketCategorieMap.get(programJoinShowTime.getId()))
                    .map(TicketCategoryAggregate::getMaxPrice).orElse(null));
            return programListVo;
        });
    }

    /**
     * 查询节目详情
     *
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public ProgramVo detail(ProgramGetDto programGetDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_DETAIL_CHECK.getValue(), programGetDto);
        return getDetail(programGetDto);
    }

    /**
     * 查询节目详情V1
     *
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public ProgramVo detailV1(ProgramGetDto programGetDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_DETAIL_CHECK.getValue(), programGetDto);
        return getDetail(programGetDto);
    }

    /**
     * 查询节目详情V2
     *
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public ProgramVo detailV2(ProgramGetDto programGetDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_DETAIL_CHECK.getValue(), programGetDto);
        return getDetailV2(programGetDto);
    }


    /**
     * 查询节目详情执行
     *
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public ProgramVo getDetail(ProgramGetDto programGetDto) {
        // 设置演出时间
        ProgramShowTime programShowTime = programShowTimeService.selectProgramShowTimeByProgramId(programGetDto.getId());
        // 构建programVo
        ProgramVo programVo = programService.getById(programGetDto.getId(), DateUtils.countBetweenSecond(DateUtils.now(),
                programShowTime.getShowTime()), TimeUnit.SECONDS);
        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());

        // 设置节目分组信息
        ProgramGroupVo programGroupVo = programService.getProgramGroup(programVo.getProgramGroupId());
        programVo.setProgramGroupVo(programGroupVo);

        // 预热购票人信息
        preloadTicketUserList(programVo.getHighHeat());

        // 预热对应账户订单信息
        preloadAccountOrderCount(programVo.getId());

        // 设置节目类型/父类型
        ProgramCategory programCategory = getProgramCategory(programVo.getProgramCategoryId());
        if (Objects.nonNull(programCategory)) {
            programVo.setProgramCategoryName(programCategory.getName());
        }
        ProgramCategory parentProgramCategory = getProgramCategory(programVo.getParentProgramCategoryId());
        if (Objects.nonNull(parentProgramCategory)) {
            programVo.setParentProgramCategoryName(parentProgramCategory.getName());
        }

        // 构建，然后节目Vo
        List<TicketCategoryVo> ticketCategoryVoList =
                ticketCategoryService.selectTicketCategoryListByProgramId(programVo.getId(),
                        DateUtils.countBetweenSecond(DateUtils.now(), programShowTime.getShowTime()), TimeUnit.SECONDS);
        programVo.setTicketCategoryVoList(ticketCategoryVoList);

        return programVo;
    }

    /**
     * 查询节目详情V2执行
     *
     * @param programGetDto 查询节目数据的入参
     * @return 执行后的结果
     */
    public ProgramVo getDetailV2(ProgramGetDto programGetDto) {
        // 查询节目演出时间
        ProgramShowTime programShowTime =
                programShowTimeService.selectProgramShowTimeByProgramIdMultipleCache(programGetDto.getId());

        ProgramVo programVo = programService.getByIdMultipleCache(programGetDto.getId(), programShowTime.getShowTime());

        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());

        // 查询节目分组信息
        ProgramGroupVo programGroupVo = programService.getProgramGroupMultipleCache(programVo.getProgramGroupId());
        programVo.setProgramGroupVo(programGroupVo);

        preloadTicketUserList(programVo.getHighHeat());

        preloadAccountOrderCount(programVo.getId());

        // 查询节目类型信息
        ProgramCategory programCategory = getProgramCategoryMultipleCache(programVo.getProgramCategoryId());
        if (Objects.nonNull(programCategory)) {
            programVo.setProgramCategoryName(programCategory.getName());
        }
        // 查询父类节目信息
        ProgramCategory parentProgramCategory = getProgramCategoryMultipleCache(programVo.getParentProgramCategoryId());
        if (Objects.nonNull(parentProgramCategory)) {
            programVo.setParentProgramCategoryName(parentProgramCategory.getName());
        }

        // 查询节目票档信息
        List<TicketCategoryVo> ticketCategoryVoList = ticketCategoryService
                .selectTicketCategoryListByProgramIdMultipleCache(programVo.getId(), programShowTime.getShowTime());
        programVo.setTicketCategoryVoList(ticketCategoryVoList);

        return programVo;
    }


    /**
     * 查询节目表详情执行（多级）
     *
     * @param programId 节目id
     * @param showTime  节目演出时间
     * @return 执行后的结果
     */
    public ProgramVo getByIdMultipleCache(Long programId, Date showTime) {
        return localCacheProgram.getCache(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId).getRelKey(),
                key -> {
                    log.info("查询节目详情 从本地缓存没有查询到 节目id : {}", programId);
                    ProgramVo programVo = getById(programId, DateUtils.countBetweenSecond(DateUtils.now(), showTime),
                            TimeUnit.SECONDS);
                    programVo.setShowTime(showTime);
                    return programVo;
                });
    }

    public ProgramVo simpleGetByIdMultipleCache(Long programId) {
        ProgramVo programVoCache = localCacheProgram.getCache(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM,
                programId).getRelKey());
        if (Objects.nonNull(programVoCache)) {
            return programVoCache;
        }
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId), ProgramVo.class);
    }

    public ProgramVo simpleGetProgramAndShowMultipleCache(Long programId) {
        ProgramShowTime programShowTime =
                programShowTimeService.simpleSelectProgramShowTimeByProgramIdMultipleCache(programId);
        if (Objects.isNull(programShowTime)) {
            throw new DaMaiFrameException(BaseCode.PROGRAM_SHOW_TIME_NOT_EXIST);
        }

        ProgramVo programVo = simpleGetByIdMultipleCache(programId);
        if (Objects.isNull(programVo)) {
            throw new DaMaiFrameException(BaseCode.PROGRAM_NOT_EXIST);
        }

        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());

        return programVo;
    }

    @ServiceLock(lockType = LockType.Read, name = PROGRAM_LOCK, keys = {"#programId"})
    public ProgramVo getById(Long programId, Long expireTime, TimeUnit timeUnit) {
        ProgramVo programVo =
                redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId), ProgramVo.class);
        if (Objects.nonNull(programVo)) {
            return programVo;
        }
        log.info("查询节目详情 从Redis缓存没有查询到 节目id : {}", programId);
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_PROGRAM_LOCK, new String[]{String.valueOf(programId)});
        lock.lock();
        try {
            return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId)
                    , ProgramVo.class,
                    () -> createProgramVo(programId)
                    , expireTime,
                    timeUnit);
        } finally {
            lock.unlock();
        }
    }

    public ProgramGroupVo getProgramGroupMultipleCache(Long programGroupId) {
        return localCacheProgramGroup.getCache(
                RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId).getRelKey(),
                key -> getProgramGroup(programGroupId));
    }

    @ServiceLock(lockType = LockType.Read, name = PROGRAM_GROUP_LOCK, keys = {"#programGroupId"})
    public ProgramGroupVo getProgramGroup(Long programGroupId) {
        ProgramGroupVo programGroupVo =
                redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId), ProgramGroupVo.class);
        if (Objects.nonNull(programGroupVo)) {
            return programGroupVo;
        }
        // 也是用到了双重锁检测
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_PROGRAM_LOCK, new String[]{String.valueOf(programGroupId)});
        lock.lock();
        try {
            programGroupVo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId),
                    ProgramGroupVo.class);
            if (Objects.isNull(programGroupVo)) {
                programGroupVo = createProgramGroupVo(programGroupId);
                redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programGroupId), programGroupVo,
                        DateUtils.countBetweenSecond(DateUtils.now(), programGroupVo.getRecentShowTime()), TimeUnit.SECONDS);
            }
            return programGroupVo;
        } finally {
            lock.unlock();
        }
    }


    public Map<Long, TicketCategoryAggregate> selectTicketCategorieMap(List<Long> programIdList) {
        List<TicketCategoryAggregate> ticketCategorieList = ticketCategoryMapper.selectAggregateList(programIdList);
        return ticketCategorieList
                .stream()
                .collect(Collectors.toMap(TicketCategoryAggregate::getProgramId,
                        ticketCategory -> ticketCategory, (v1, v2) -> v2));
    }

    @RepeatExecuteLimit(name = CANCEL_PROGRAM_ORDER, keys = {"#programOperateDataDto.programId", "#programOperateDataDto.seatIdList"})
    @Transactional(rollbackFor = Exception.class)
    public void operateProgramData(ProgramOperateDataDto programOperateDataDto) {
        List<TicketCategoryCountDto> ticketCategoryCountDtoList = programOperateDataDto.getTicketCategoryCountDtoList();
        List<Long> seatIdList = programOperateDataDto.getSeatIdList();
        LambdaQueryWrapper<Seat> seatLambdaQueryWrapper =
                Wrappers.lambdaQuery(Seat.class)
                        .eq(Seat::getProgramId, programOperateDataDto.getProgramId())
                        .in(Seat::getId, seatIdList);
        List<Seat> seatList = seatMapper.selectList(seatLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(seatList)) {
            throw new DaMaiFrameException(BaseCode.SEAT_NOT_EXIST);
        }
        if (seatList.size() != seatIdList.size()) {
            throw new DaMaiFrameException(BaseCode.SEAT_UPDATE_REL_COUNT_NOT_EQUAL_PRESET_COUNT);
        }
        for (Seat seat : seatList) {
            if (Objects.equals(seat.getSellStatus(), SellStatus.SOLD.getCode())) {
                throw new DaMaiFrameException(BaseCode.SEAT_SOLD);
            }
        }
        LambdaUpdateWrapper<Seat> seatLambdaUpdateWrapper =
                Wrappers.lambdaUpdate(Seat.class)
                        .eq(Seat::getProgramId, programOperateDataDto.getProgramId())
                        .in(Seat::getId, seatIdList);
        Seat updateSeat = new Seat();
        updateSeat.setSellStatus(SellStatus.SOLD.getCode());
        seatMapper.update(updateSeat, seatLambdaUpdateWrapper);

        int updateRemainNumberCount =
                ticketCategoryMapper.batchUpdateRemainNumber(ticketCategoryCountDtoList, programOperateDataDto.getProgramId());
        if (updateRemainNumberCount != ticketCategoryCountDtoList.size()) {
            throw new DaMaiFrameException(BaseCode.UPDATE_TICKET_CATEGORY_COUNT_NOT_CORRECT);
        }
    }

    private ProgramVo createProgramVo(Long programId) {
        ProgramVo programVo = new ProgramVo();
        Program program =
                Optional.ofNullable(programMapper.selectById(programId))
                        .orElseThrow(() -> new DaMaiFrameException(BaseCode.PROGRAM_NOT_EXIST));
        BeanUtil.copyProperties(program, programVo);
        AreaGetDto areaGetDto = new AreaGetDto();
        areaGetDto.setId(program.getAreaId());
        ApiResponse<AreaVo> areaResponse = baseDataClient.getById(areaGetDto);
        if (Objects.equals(areaResponse.getCode(), com.damai.common.ApiResponse.ok().getCode())) {
            if (Objects.nonNull(areaResponse.getData())) {
                programVo.setAreaName(areaResponse.getData().getName());
            }
        } else {
            log.error("base-data rpc getById error areaResponse:{}", JSON.toJSONString(areaResponse));
        }
        return programVo;
    }

    private ProgramGroupVo createProgramGroupVo(Long programGroupId) {
        ProgramGroupVo programGroupVo = new ProgramGroupVo();
        ProgramGroup programGroup =
                Optional.ofNullable(programGroupMapper.selectById(programGroupId))
                        .orElseThrow(() -> new DaMaiFrameException(BaseCode.PROGRAM_GROUP_NOT_EXIST));
        programGroupVo.setId(programGroup.getId());
        programGroupVo.setProgramSimpleInfoVoList(JSON.parseArray(programGroup.getProgramJson(), ProgramSimpleInfoVo.class));
        programGroupVo.setRecentShowTime(programGroup.getRecentShowTime());
        return programGroupVo;
    }

    public List<Long> getAllProgramIdList() {
        LambdaQueryWrapper<Program> programLambdaQueryWrapper =
                Wrappers.lambdaQuery(Program.class).eq(Program::getProgramStatus, BusinessStatus.YES.getCode())
                        .select(Program::getId);
        List<Program> programs = programMapper.selectList(programLambdaQueryWrapper);
        return programs.stream().map(Program::getId).collect(Collectors.toList());
    }

    public ProgramVo getDetailFromDb(Long programId) {
        ProgramVo programVo = createProgramVo(programId);

        ProgramCategory programCategory = getProgramCategory(programVo.getProgramCategoryId());
        if (Objects.nonNull(programCategory)) {
            programVo.setProgramCategoryName(programCategory.getName());
        }
        ProgramCategory parentProgramCategory = getProgramCategory(programVo.getParentProgramCategoryId());
        if (Objects.nonNull(parentProgramCategory)) {
            programVo.setParentProgramCategoryName(parentProgramCategory.getName());
        }

        LambdaQueryWrapper<ProgramShowTime> programShowTimeLambdaQueryWrapper =
                Wrappers.lambdaQuery(ProgramShowTime.class).eq(ProgramShowTime::getProgramId, programId);
        ProgramShowTime programShowTime = Optional.ofNullable(programShowTimeMapper.selectOne(programShowTimeLambdaQueryWrapper))
                .orElseThrow(() -> new DaMaiFrameException(BaseCode.PROGRAM_SHOW_TIME_NOT_EXIST));

        programVo.setShowTime(programShowTime.getShowTime());
        programVo.setShowDayTime(programShowTime.getShowDayTime());
        programVo.setShowWeekTime(programShowTime.getShowWeekTime());

        return programVo;
    }

    private void preloadTicketUserList(Integer highHeat) {
        if (Objects.equals(highHeat, BusinessStatus.NO.getCode())) {
            return;
        }
        String userId = BaseParameterHolder.getParameter(USER_ID);
        String code = BaseParameterHolder.getParameter(CODE);
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(code)) {
            return;
        }
        Boolean userLogin =
                redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, userId));
        if (!userLogin) {
            return;
        }
        BusinessThreadPool.execute(() -> {
            try {
                if (!redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.TICKET_USER_LIST, userId))) {
                    TicketUserListDto ticketUserListDto = new TicketUserListDto();
                    ticketUserListDto.setUserId(Long.parseLong(userId));
                    ApiResponse<List<TicketUserVo>> apiResponse = userClient.list(ticketUserListDto);
                    if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                        Optional.ofNullable(apiResponse.getData()).filter(CollectionUtil::isNotEmpty)
                                .ifPresent(ticketUserVoList -> redisCache.set(RedisKeyBuild.createRedisKey(
                                        RedisKeyManage.TICKET_USER_LIST, userId), ticketUserVoList));
                    } else {
                        log.warn("userClient.select 调用失败 apiResponse : {}", JSON.toJSONString(apiResponse));
                    }
                }

            } catch (Exception e) {
                log.error("预热加载购票人列表失败", e);
            }
        });
    }

    private void preloadAccountOrderCount(Long programId) {
        String userId = BaseParameterHolder.getParameter(USER_ID);
        String code = BaseParameterHolder.getParameter(CODE);
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(code)) {
            return;
        }
        Boolean userLogin =
                redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, userId));
        if (!userLogin) {
            return;
        }
        BusinessThreadPool.execute(() -> {
            try {
                if (!redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT, userId, programId))) {
                    AccountOrderCountDto accountOrderCountDto = new AccountOrderCountDto();
                    accountOrderCountDto.setUserId(Long.parseLong(userId));
                    accountOrderCountDto.setProgramId(programId);
                    ApiResponse<AccountOrderCountVo> apiResponse = orderClient.accountOrderCount(accountOrderCountDto);
                    if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                        Optional.ofNullable(apiResponse.getData())
                                .ifPresent(accountOrderCountVo -> redisCache.set(
                                        RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT, userId, programId),
                                        accountOrderCountVo.getCount(), tokenExpireManager.getTokenExpireTime() + 1,
                                        TimeUnit.MINUTES));
                    } else {
                        log.warn("orderClient.accountOrderCount 调用失败 apiResponse : {}", JSON.toJSONString(apiResponse));
                    }
                }
            } catch (Exception e) {
                log.error("预热加载账户订单数量失败", e);
            }
        });
    }

    public ProgramCategory getProgramCategoryMultipleCache(Long programCategoryId) {
        return localCacheProgramCategory.get(String.valueOf(programCategoryId),
                key -> getProgramCategory(programCategoryId));
    }

    public ProgramCategory getProgramCategory(Long programCategoryId) {
        return programCategoryService.getProgramCategory(programCategoryId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean resetExecute(ProgramResetExecuteDto programResetExecuteDto) {
        Long programId = programResetExecuteDto.getProgramId();
        LambdaQueryWrapper<Seat> seatQueryWrapper =
                Wrappers.lambdaQuery(Seat.class).eq(Seat::getProgramId, programId)
                        .in(Seat::getSellStatus, SellStatus.LOCK.getCode(), SellStatus.SOLD.getCode());
        List<Seat> seatList = seatMapper.selectList(seatQueryWrapper);
        if (CollectionUtil.isEmpty(seatList)) {
            return true;
        }
        LambdaUpdateWrapper<Seat> seatUpdateWrapper =
                Wrappers.lambdaUpdate(Seat.class).eq(Seat::getProgramId, programId);
        Seat seatUpdate = new Seat();
        seatUpdate.setSellStatus(SellStatus.NO_SOLD.getCode());
        seatMapper.update(seatUpdate, seatUpdateWrapper);

        LambdaQueryWrapper<TicketCategory> ticketCategoryQueryWrapper =
                Wrappers.lambdaQuery(TicketCategory.class).eq(TicketCategory::getProgramId, programId);
        List<TicketCategory> ticketCategories = ticketCategoryMapper.selectList(ticketCategoryQueryWrapper);
        for (TicketCategory ticketCategory : ticketCategories) {
            Long remainNumber = ticketCategory.getRemainNumber();
            Long totalNumber = ticketCategory.getTotalNumber();
            if (!(remainNumber.equals(totalNumber))) {
                TicketCategory ticketCategoryUpdate = new TicketCategory();
                ticketCategoryUpdate.setRemainNumber(totalNumber);

                LambdaUpdateWrapper<TicketCategory> ticketCategoryUpdateWrapper =
                        Wrappers.lambdaUpdate(TicketCategory.class)
                                .eq(TicketCategory::getProgramId, programId)
                                .eq(TicketCategory::getId, ticketCategory.getId());
                ticketCategoryMapper.update(ticketCategoryUpdate, ticketCategoryUpdateWrapper);
            }
        }
        delRedisData(programId);
        delLocalCache(programId);
        return true;
    }

    public void delRedisData(Long programId) {
        Program program = Optional.ofNullable(programMapper.selectById(programId))
                .orElseThrow(() -> new DaMaiFrameException(BaseCode.PROGRAM_NOT_EXIST));
        List<String> keys = new ArrayList<>();
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, program.getProgramGroupId()).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME, programId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH, programId, "*").getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH, programId, "*").getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_SOLD_RESOLUTION_HASH, programId, "*").getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_CATEGORY_LIST, programId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION, programId, "*").getRelKey());
        programDelCacheData.del(keys, new String[]{});
    }

    public Boolean invalid(@NotNull final ProgramInvalidDto programInvalidDto) {
        Program program = new Program();
        program.setId(programInvalidDto.getId());
        program.setProgramStatus(BusinessStatus.NO.getCode());
        int result = programMapper.updateById(program);
        if (result > 0) {
            // 删除redis中的数据
            delRedisData(programInvalidDto.getId());
            // 删除本地缓存
            redisStreamPushHandler.push(String.valueOf(programInvalidDto.getId()));
            // 删除ES
            programEs.deleteByProgramId(programInvalidDto.getId());
            return true;
        } else {
            return false;
        }
    }

    public ProgramVo localDetail(@NotNull final ProgramGetDto programGetDto) {
        return localCacheProgram.getCache(String.valueOf(programGetDto.getId()));
    }

    public void delLocalCache(Long programId) {
        log.info("删除本地缓存 programId : {}", programId);
        localCacheProgram.del(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId).getRelKey());
        localCacheProgramGroup.del(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_GROUP, programId).getRelKey());
        localCacheProgramShowTime.del(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME, programId).getRelKey());
        localCacheTicketCategory.del(programId);
    }

}
