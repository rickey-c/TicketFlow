package com.ticketflow.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.ProgramShowTimeAddDto;
import com.ticketflow.entity.Program;
import com.ticketflow.entity.ProgramGroup;
import com.ticketflow.entity.ProgramShowTime;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.mapper.ProgramGroupMapper;
import com.ticketflow.mapper.ProgramMapper;
import com.ticketflow.mapper.ProgramShowTimeMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.service.cache.local.LocalCacheProgramShowTime;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.util.ServiceLockTool;
import com.ticketflow.utils.DateUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ticketflow.core.DistributedLockConstants.GET_PROGRAM_SHOW_TIME_LOCK;
import static com.ticketflow.core.DistributedLockConstants.PROGRAM_SHOW_TIME_LOCK;

/**
 * @Description: 节目演出时间 service
 * @Author: rickey-c
 * @Date: 2025/2/3 20:55
 */
@Service
public class ProgramShowTimeService extends ServiceImpl<ProgramShowTimeMapper, ProgramShowTime> {

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private ProgramShowTimeMapper programShowTimeMapper;

    @Autowired
    private ProgramGroupMapper programGroupMapper;

    @Autowired
    private ServiceLockTool serviceLockTool;

    @Autowired
    private LocalCacheProgramShowTime localCacheProgramShowTime;


    @Transactional(rollbackFor = Exception.class)
    public Long add(ProgramShowTimeAddDto programShowTimeAddDto) {
        ProgramShowTime programShowTime = new ProgramShowTime();
        BeanUtil.copyProperties(programShowTimeAddDto, programShowTime);
        programShowTime.setId(uidGenerator.getUid());
        programShowTimeMapper.insert(programShowTime);
        return programShowTime.getId();
    }

    public com.ticketflow.entity.ProgramShowTime selectProgramShowTimeByProgramIdMultipleCache(Long programId) {
        return localCacheProgramShowTime.getCache(RedisKeyBuild.createRedisKey
                        (RedisKeyManage.PROGRAM_SHOW_TIME, programId).getRelKey(),
                key -> selectProgramShowTimeByProgramId(programId));
    }

    public ProgramShowTime simpleSelectProgramShowTimeByProgramIdMultipleCache(Long programId) {
        ProgramShowTime programShowTimeCache = localCacheProgramShowTime.getCache(RedisKeyBuild.createRedisKey(
                RedisKeyManage.PROGRAM_SHOW_TIME, programId).getRelKey());
        if (Objects.nonNull(programShowTimeCache)) {
            return programShowTimeCache;
        }
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME,
                programId), ProgramShowTime.class);
    }

    @ServiceLock(lockType = LockType.Read, name = PROGRAM_SHOW_TIME_LOCK, keys = {"#programId"})
    public ProgramShowTime selectProgramShowTimeByProgramId(Long programId) {
        // 从缓存查询
        ProgramShowTime programShowTime = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME,
                programId), ProgramShowTime.class);
        // 查到了就返回，查不到就上锁准备从数据库查询
        if (Objects.nonNull(programShowTime)) {
            return programShowTime;
        }
        // 加锁
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_PROGRAM_SHOW_TIME_LOCK,
                new String[]{String.valueOf(programId)});
        lock.lock();
        try {
            // 再此尝试从缓存中查询，这样保证之后第一个请求走数据库，其他请求在后续都会通过缓存查到数据
            // 优化过期时间，缓存的过期时间即最近的节目演出时间
            programShowTime = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME,
                    programId), ProgramShowTime.class);
            // 缓存还查不到，只能从数据库查询，在放入缓存中
            if (Objects.isNull(programShowTime)) {
                LambdaQueryWrapper<ProgramShowTime> programShowTimeLambdaQueryWrapper =
                        Wrappers.lambdaQuery(ProgramShowTime.class).eq(ProgramShowTime::getProgramId, programId);
                programShowTime = Optional.ofNullable(programShowTimeMapper.selectOne(programShowTimeLambdaQueryWrapper))
                        .orElseThrow(() -> new TicketFlowFrameException(BaseCode.PROGRAM_SHOW_TIME_NOT_EXIST));
                redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME, programId), programShowTime
                        , DateUtils.countBetweenSecond(DateUtils.now(), programShowTime.getShowTime()), TimeUnit.SECONDS);
            }
            return programShowTime;
        } finally {
            lock.unlock();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Set<Long> renewal() {
        Set<Long> programIdSet = new HashSet<>();
        LambdaQueryWrapper<ProgramShowTime> programShowTimeLambdaQueryWrapper =
                Wrappers.lambdaQuery(ProgramShowTime.class).
                        le(ProgramShowTime::getShowTime, DateUtils.addDay(DateUtils.now(), 2));
        List<ProgramShowTime> programShowTimes = programShowTimeMapper.selectList(programShowTimeLambdaQueryWrapper);

        List<ProgramShowTime> newProgramShowTimes = new ArrayList<>(programShowTimes.size());

        for (ProgramShowTime programShowTime : programShowTimes) {
            programIdSet.add(programShowTime.getProgramId());
            Date oldShowTime = programShowTime.getShowTime();
            Date newShowTime = DateUtils.addMonth(oldShowTime, 1);
            Date nowDateTime = DateUtils.now();
            while (newShowTime.before(nowDateTime)) {
                newShowTime = DateUtils.addMonth(newShowTime, 1);
            }
            Date newShowDayTime = DateUtils.parseDateTime(DateUtils.formatDate(newShowTime) + " 00:00:00");
            ProgramShowTime updateProgramShowTime = new ProgramShowTime();
            updateProgramShowTime.setShowTime(newShowTime);
            updateProgramShowTime.setShowDayTime(newShowDayTime);
            updateProgramShowTime.setShowWeekTime(DateUtils.getWeekStr(newShowTime));
            LambdaUpdateWrapper<ProgramShowTime> programShowTimeLambdaUpdateWrapper =
                    Wrappers.lambdaUpdate(ProgramShowTime.class)
                            .eq(ProgramShowTime::getProgramId, programShowTime.getProgramId())
                            .eq(ProgramShowTime::getId, programShowTime.getId());

            programShowTimeMapper.update(updateProgramShowTime, programShowTimeLambdaUpdateWrapper);

            ProgramShowTime newProgramShowTime = new ProgramShowTime();
            newProgramShowTime.setProgramId(programShowTime.getProgramId());
            newProgramShowTime.setShowTime(newShowTime);
            newProgramShowTimes.add(newProgramShowTime);
        }
        Map<Long, Date> programGroupMap = new HashMap<>(newProgramShowTimes.size());
        for (ProgramShowTime newProgramShowTime : newProgramShowTimes) {
            Program program = programMapper.selectById(newProgramShowTime.getProgramId());
            if (Objects.isNull(program)) {
                continue;
            }
            Long programGroupId = program.getProgramGroupId();
            Date showTime = programGroupMap.get(programGroupId);
            if (Objects.isNull(showTime)) {
                programGroupMap.put(programGroupId, newProgramShowTime.getShowTime());
            } else {
                if (DateUtil.compare(newProgramShowTime.getShowTime(), showTime) < 0) {
                    programGroupMap.put(programGroupId, newProgramShowTime.getShowTime());
                }
            }
        }
        if (CollectionUtil.isNotEmpty(programGroupMap)) {
            programGroupMap.forEach((k, v) -> {
                ProgramGroup programGroup = new ProgramGroup();
                programGroup.setRecentShowTime(v);

                LambdaUpdateWrapper<ProgramGroup> programGroupLambdaUpdateWrapper =
                        Wrappers.lambdaUpdate(ProgramGroup.class)
                                .eq(ProgramGroup::getId, k);
                programGroupMapper.update(programGroup, programGroupLambdaUpdateWrapper);
            });
        }

        return programIdSet;
    }
}
