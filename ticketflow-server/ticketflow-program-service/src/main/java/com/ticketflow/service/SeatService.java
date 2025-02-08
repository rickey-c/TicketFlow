package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.enums.BaseCode;
import com.damai.enums.BusinessStatus;
import com.damai.enums.SeatType;
import com.damai.enums.SellStatus;
import com.damai.exception.DaMaiFrameException;
import com.damai.util.DateUtils;
import com.ticketflow.ProgramShowTimeService;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.*;
import com.ticketflow.entity.ProgramShowTime;
import com.ticketflow.entity.Seat;
import com.ticketflow.mapper.SeatMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.service.lua.ProgramSeatCacheData;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.util.ServiceLockTool;
import com.ticketflow.vo.ProgramVo;
import com.ticketflow.vo.SeatRelateInfoVo;
import com.ticketflow.vo.SeatVo;
import com.ticketflow.vo.TicketCategoryVo;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ticketflow.core.DistributedLockConstants.GET_SEAT_LOCK;
import static com.ticketflow.core.DistributedLockConstants.SEAT_LOCK;

/**
 * @Description: 座位服务
 * @Author: rickey-c
 * @Date: 2025/2/7 23:56
 */
public class SeatService extends ServiceImpl<SeatMapper, Seat> {

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramShowTimeService programShowTimeService;

    @Autowired
    private ServiceLockTool serviceLockTool;

    @Autowired
    private TicketCategoryService ticketCategoryService;

    @Autowired
    private ProgramSeatCacheData programSeatCacheData;

    public Long add(SeatAddDto seatAddDto) {
        LambdaQueryWrapper<Seat> seatLambdaQueryWrapper = Wrappers.lambdaQuery(Seat.class)
                .eq(Seat::getProgramId, seatAddDto.getProgramId())
                .eq(Seat::getRowCode, seatAddDto.getRowCode())
                .eq(Seat::getColCode, seatAddDto.getColCode());
        Seat seat = seatMapper.selectOne(seatLambdaQueryWrapper);
        if (Objects.nonNull(seat)) {
            throw new DaMaiFrameException(BaseCode.SEAT_IS_EXIST);
        }
        seat = new Seat();
        BeanUtil.copyProperties(seatAddDto, seat);
        seat.setId(uidGenerator.getUid());
        seatMapper.insert(seat);
        return seat.getId();
    }

    public List<SeatVo> getSeatVoListByCacheResolution(Long programId, Long ticketCategoryId) {
        List<String> keys = new ArrayList<>(4);
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH,
                programId, ticketCategoryId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH,
                programId, ticketCategoryId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_SOLD_RESOLUTION_HASH,
                programId, ticketCategoryId).getRelKey());
        return programSeatCacheData.getData(keys, new String[]{});
    }

    @ServiceLock(lockType = LockType.Read, name = SEAT_LOCK, keys = {"#programId", "#ticketCategoryId"})
    public List<SeatVo> selectSeatResolution(Long programId, Long ticketCategoryId, Long expireTime, TimeUnit timeUnit) {
        //从缓存中查询所有状态的座位(原子性)
        List<SeatVo> seatVoList = getSeatVoListByCacheResolution(programId, ticketCategoryId);
        if (CollectionUtil.isNotEmpty(seatVoList)) {
            return seatVoList;
        }
        //如果redis中三种状态的座位都没有，说明根本就没有往redis中存放过
        //加锁
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_SEAT_LOCK, new String[]{String.valueOf(programId),
                String.valueOf(ticketCategoryId)});
        lock.lock();
        try {
            //再从缓存中查询座位数据
            seatVoList = getSeatVoListByCacheResolution(programId, ticketCategoryId);
            if (CollectionUtil.isNotEmpty(seatVoList)) {
                return seatVoList;
            }
            //如果缓存中还是没有，则从数据库中查询
            LambdaQueryWrapper<Seat> seatLambdaQueryWrapper =
                    Wrappers.lambdaQuery(Seat.class).eq(Seat::getProgramId, programId)
                            .eq(Seat::getTicketCategoryId, ticketCategoryId);
            List<Seat> seats = seatMapper.selectList(seatLambdaQueryWrapper);
            for (Seat seat : seats) {
                SeatVo seatVo = new SeatVo();
                BeanUtil.copyProperties(seat, seatVo);
                seatVo.setSeatTypeName(SeatType.getMsg(seat.getSeatType()));
                seatVoList.add(seatVo);
            }
            //将座位按照状态进行分类
            Map<Integer, List<SeatVo>> seatMap = seatVoList.stream().collect(Collectors.groupingBy(SeatVo::getSellStatus));
            //没有售卖的座位
            List<SeatVo> noSoldSeatVoList = seatMap.get(SellStatus.NO_SOLD.getCode());
            //正在锁定的座位
            List<SeatVo> lockSeatVoList = seatMap.get(SellStatus.LOCK.getCode());
            //已经售卖的座位
            List<SeatVo> soldSeatVoList = seatMap.get(SellStatus.SOLD.getCode());
            if (CollectionUtil.isNotEmpty(noSoldSeatVoList)) {
                //将没有售卖的座位放入redis中
                redisCache.putHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH,
                                programId, ticketCategoryId), noSoldSeatVoList.stream()
                                .collect(Collectors.toMap(s -> String.valueOf(s.getId()), s -> s, (v1, v2) -> v2))
                        , expireTime, timeUnit);
            }
            if (CollectionUtil.isNotEmpty(lockSeatVoList)) {
                //将正在锁定的座位放入redis中
                redisCache.putHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH,
                                programId, ticketCategoryId), lockSeatVoList.stream()
                                .collect(Collectors.toMap(s -> String.valueOf(s.getId()), s -> s, (v1, v2) -> v2))
                        , expireTime, timeUnit);
            }
            if (CollectionUtil.isNotEmpty(soldSeatVoList)) {
                //将已经售卖的座位放入redis中
                redisCache.putHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_SOLD_RESOLUTION_HASH,
                                programId, ticketCategoryId)
                        , soldSeatVoList.stream()
                                .collect(Collectors.toMap(s -> String.valueOf(s.getId()), s -> s, (v1, v2) -> v2))
                        , expireTime, timeUnit);
            }
            //将座位集合，先按座位行号排序，再接着按座位列号排序
            seatVoList = seatVoList.stream().sorted(Comparator.comparingInt(SeatVo::getRowCode)
                    .thenComparingInt(SeatVo::getColCode)).collect(Collectors.toList());
            return seatVoList;
        } finally {
            lock.unlock();
        }
    }

    public SeatRelateInfoVo relateInfo(SeatListDto seatListDto) {
        SeatRelateInfoVo seatRelateInfoVo = new SeatRelateInfoVo();
        //从redis中查询节目
        ProgramVo programVo =
                redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, seatListDto.getProgramId()), ProgramVo.class);
        //如果查询不到，则去数据库中查询，再放入redis中
        if (Objects.isNull(programVo)) {
            ProgramGetDto programGetDto = new ProgramGetDto();
            programGetDto.setId(seatListDto.getProgramId());
            programVo = programService.detail(programGetDto);
        }
        //查询演出时间
        ProgramShowTime programShowTime = programShowTimeService.selectProgramShowTimeByProgramId(seatListDto.getProgramId());
        //查询该节目下的票档集合
        List<TicketCategoryVo> ticketCategoryVoList = ticketCategoryService
                .selectTicketCategoryListByProgramIdMultipleCache(programVo.getId(), programShowTime.getShowTime());

        List<SeatVo> seatVos = new ArrayList<>();
        //遍历票档集合
        for (TicketCategoryVo ticketCategoryVo : ticketCategoryVoList) {
            //根据节目id和票档id来查询出座位集合，汇总到seatVos中
            seatVos.addAll(selectSeatResolution(seatListDto.getProgramId(), ticketCategoryVo.getId(),
                    DateUtils.countBetweenSecond(DateUtils.now(), programShowTime.getShowTime()), TimeUnit.SECONDS));
        }
        //如果节目不允许选择座位，直接抛出异常
        if (programVo.getPermitChooseSeat().equals(BusinessStatus.NO.getCode())) {
            throw new DaMaiFrameException(BaseCode.PROGRAM_NOT_ALLOW_CHOOSE_SEAT);
        }

        Map<String, List<SeatVo>> seatVoMap =
                seatVos.stream().collect(Collectors.groupingBy(seatVo -> seatVo.getPrice().toString()));
        //节目id
        seatRelateInfoVo.setProgramId(programVo.getId());
        //节目地点
        seatRelateInfoVo.setPlace(programVo.getPlace());
        //节目演出时间
        seatRelateInfoVo.setShowTime(programShowTime.getShowTime());
        //节目演出时间对应的星期
        seatRelateInfoVo.setShowWeekTime(programShowTime.getShowWeekTime());
        //节目的价格列表
        seatRelateInfoVo.setPriceList(seatVoMap.keySet().stream().sorted().collect(Collectors.toList()));
        //节目的座位列表
        seatRelateInfoVo.setSeatVoMap(seatVoMap);
        return seatRelateInfoVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(SeatBatchAddDto seatBatchAddDto) {
        Long programId = seatBatchAddDto.getProgramId();
        List<SeatBatchRelateInfoAddDto> seatBatchRelateInfoAddDtoList = seatBatchAddDto.getSeatBatchRelateInfoAddDtoList();
        int rowIndex = 0;
        for (SeatBatchRelateInfoAddDto seatBatchRelateInfoAddDto : seatBatchRelateInfoAddDtoList) {
            Long ticketCategoryId = seatBatchRelateInfoAddDto.getTicketCategoryId();
            BigDecimal price = seatBatchRelateInfoAddDto.getPrice();
            Integer count = seatBatchRelateInfoAddDto.getCount();

            int colCount = 10;
            int rowCount = count / colCount;

            for (int i = 1; i <= rowCount; i++) {
                rowIndex++;
                for (int j = 1; j <= colCount; j++) {
                    Seat seat = new Seat();
                    seat.setProgramId(programId);
                    seat.setTicketCategoryId(ticketCategoryId);
                    seat.setRowCode(rowIndex);
                    seat.setColCode(j);
                    seat.setSeatType(1);
                    seat.setPrice(price);
                    seat.setSellStatus(SellStatus.NO_SOLD.getCode());
                    seatMapper.insert(seat);
                }
            }
        }
        return true;
    }


}
