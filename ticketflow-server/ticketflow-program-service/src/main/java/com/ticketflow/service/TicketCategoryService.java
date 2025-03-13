package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.utils.DateUtils;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.TicketCategoryAddDto;
import com.ticketflow.dto.TicketCategoryDto;
import com.ticketflow.entity.TicketCategory;
import com.ticketflow.mapper.TicketCategoryMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.service.cache.local.LocalCacheTicketCategory;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.util.ServiceLockTool;
import com.ticketflow.vo.TicketCategoryDetailVo;
import com.ticketflow.vo.TicketCategoryVo;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ticketflow.core.DistributedLockConstants.*;

/**
 * @Description: 票档 service
 * @Author: rickey-c
 * @Date: 2025/2/7 23:30
 */
public class TicketCategoryService extends ServiceImpl<TicketCategoryMapper, TicketCategory> {

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TicketCategoryMapper ticketCategoryMapper;

    @Autowired
    private ServiceLockTool serviceLockTool;

    @Autowired
    private LocalCacheTicketCategory localCacheTicketCategory;

    @Transactional(rollbackFor = Exception.class)
    public Long add(TicketCategoryAddDto ticketCategoryAddDto) {
        TicketCategory ticketCategory = new TicketCategory();
        BeanUtil.copyProperties(ticketCategoryAddDto, ticketCategory);
        ticketCategory.setId(uidGenerator.getUid());
        ticketCategoryMapper.insert(ticketCategory);
        return ticketCategory.getId();
    }

    public List<TicketCategoryVo> selectTicketCategoryListByProgramIdMultipleCache(Long programId, Date showTime) {
        return localCacheTicketCategory.getCache(programId,
                key -> selectTicketCategoryListByProgramId(programId,
                        DateUtils.countBetweenSecond(DateUtils.now(), showTime),
                        TimeUnit.SECONDS));
    }

    @ServiceLock(lockType = LockType.Read, name = TICKET_CATEGORY_LOCK, keys = {"#programId"})
    public List<TicketCategoryVo> selectTicketCategoryListByProgramId(Long programId, Long expireTime, TimeUnit timeUnit) {
        List<TicketCategoryVo> ticketCategoryVoList =
                redisCache.getValueIsList(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_CATEGORY_LIST,
                        programId), TicketCategoryVo.class);
        if (CollectionUtil.isNotEmpty(ticketCategoryVoList)) {
            return ticketCategoryVoList;
        }
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_TICKET_CATEGORY_LOCK,
                new String[]{String.valueOf(programId)});
        lock.lock();
        try {
            return redisCache.getValueIsList(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_CATEGORY_LIST, programId),
                    TicketCategoryVo.class,
                    // supplier接口执行list填充逻辑，最后放到缓存中
                    () -> {
                        LambdaQueryWrapper<TicketCategory> ticketCategoryLambdaQueryWrapper =
                                Wrappers.lambdaQuery(TicketCategory.class).eq(TicketCategory::getProgramId, programId);
                        List<TicketCategory> ticketCategoryList =
                                ticketCategoryMapper.selectList(ticketCategoryLambdaQueryWrapper);
                        return ticketCategoryList.stream().map(ticketCategory -> {
                            ticketCategory.setRemainNumber(null);
                            TicketCategoryVo ticketCategoryVo = new TicketCategoryVo();
                            BeanUtil.copyProperties(ticketCategory, ticketCategoryVo);
                            return ticketCategoryVo;
                        }).collect(Collectors.toList());
                    }, expireTime, timeUnit);
        } finally {
            lock.unlock();
        }
    }

    @ServiceLock(lockType = LockType.Read, name = REMAIN_NUMBER_LOCK, keys = {"#programId", "#ticketCategoryId"})
    public Map<String, Long> getRedisRemainNumberResolution(Long programId, Long ticketCategoryId) {
        // 根据节目id和票档id进行数据分片
        Map<String, Long> ticketCategoryRemainNumber = redisCache.getAllMapForHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION,
                programId, ticketCategoryId), Long.class);

        if (CollectionUtil.isNotEmpty(ticketCategoryRemainNumber)) {
            return ticketCategoryRemainNumber;
        }
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_REMAIN_NUMBER_LOCK,
                new String[]{String.valueOf(programId), String.valueOf(ticketCategoryId)});
        lock.lock();
        try {
            ticketCategoryRemainNumber = redisCache.getAllMapForHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION,
                    programId, ticketCategoryId), Long.class);
            if (CollectionUtil.isNotEmpty(ticketCategoryRemainNumber)) {
                return ticketCategoryRemainNumber;
            }
            LambdaQueryWrapper<TicketCategory> ticketCategoryLambdaQueryWrapper = Wrappers.lambdaQuery(TicketCategory.class)
                    .eq(TicketCategory::getProgramId, programId).eq(TicketCategory::getId, ticketCategoryId);
            List<TicketCategory> ticketCategoryList = ticketCategoryMapper.selectList(ticketCategoryLambdaQueryWrapper);
            Map<String, Long> map = ticketCategoryList.stream().collect(Collectors.toMap(t -> String.valueOf(t.getId()),
                    TicketCategory::getRemainNumber, (v1, v2) -> v2));
            redisCache.putHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION, programId, ticketCategoryId), map);
            return map;
        } finally {
            lock.unlock();
        }
    }


    public TicketCategoryDetailVo detail(TicketCategoryDto ticketCategoryDto) {
        TicketCategory ticketCategory = ticketCategoryMapper.selectById(ticketCategoryDto.getId());
        TicketCategoryDetailVo ticketCategoryDetailVo = new TicketCategoryDetailVo();
        BeanUtil.copyProperties(ticketCategory, ticketCategoryDetailVo);
        return ticketCategoryDetailVo;
    }

}

