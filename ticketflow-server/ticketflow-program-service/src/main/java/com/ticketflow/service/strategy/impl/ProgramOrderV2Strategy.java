package com.ticketflow.service.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.ticketflow.base.AbstractApplicationCommandLineRunnerHandler;
import com.ticketflow.core.RepeatExecuteLimitConstants;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.dto.SeatDto;
import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.enums.ProgramOrderVersion;
import com.ticketflow.impl.composite.CompositeContainer;
import com.ticketflow.locallock.LocalLockCache;
import com.ticketflow.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketflow.service.ProgramOrderService;
import com.ticketflow.service.strategy.ProgramOrderContext;
import com.ticketflow.service.strategy.ProgramOrderStrategy;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.util.ServiceLockTool;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.ticketflow.core.DistributedLockConstants.PROGRAM_ORDER_CREATE_V2;

/**
 * @Description: 节目订单 v2
 * @Author: rickey-c
 * @Date: 2025/2/9 15:25
 */
@Slf4j
@Component
public class ProgramOrderV2Strategy extends AbstractApplicationCommandLineRunnerHandler implements ProgramOrderStrategy {

    @Autowired
    private ProgramOrderService programOrderService;

    @Autowired
    private ServiceLockTool serviceLockTool;

    @Autowired
    private CompositeContainer compositeContainer;

    @Autowired
    private LocalLockCache localLockCache;

    @RepeatExecuteLimit(
            name = RepeatExecuteLimitConstants.CREATE_PROGRAM_ORDER,
            keys = {"#programOrderCreateDto.userId", "#programOrderCreateDto.programId"})
    @Override
    public String createOrder(ProgramOrderCreateDto programOrderCreateDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue(), programOrderCreateDto);
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        List<Long> ticketCategoryIdList = new ArrayList<>();
        // 获取票档集合
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            ticketCategoryIdList =
                    seatDtoList.stream().map(SeatDto::getTicketCategoryId).distinct().collect(Collectors.toList());
        } else {
            ticketCategoryIdList.add(programOrderCreateDto.getTicketCategoryId());
        }
        // 本地锁集合
        List<ReentrantLock> localLockList = new ArrayList<>(ticketCategoryIdList.size());
        // 分布式锁集合
        List<RLock> serviceLockList = new ArrayList<>(ticketCategoryIdList.size());
        // 获取成功的本地锁集合
        List<ReentrantLock> localLockSuccessList = new ArrayList<>(ticketCategoryIdList.size());
        // 获取成功的分布式锁集合
        List<RLock> serviceLockSuccessList = new ArrayList<>(ticketCategoryIdList.size());
        // 获取锁集合
        for (Long ticketCategoryId : ticketCategoryIdList) {
            String lockKey = StrUtil.join("-", PROGRAM_ORDER_CREATE_V2,
                    programOrderCreateDto.getProgramId(), ticketCategoryId);
            ReentrantLock localLock = localLockCache.getLock(lockKey, false);
            RLock serviceLock = serviceLockTool.getLock(LockType.Reentrant, lockKey);
            localLockList.add(localLock);
            serviceLockList.add(serviceLock);
        }
        // 先逐个获取本地锁
        for (ReentrantLock reentrantLock : localLockList) {
            try {
                reentrantLock.lock();
            } catch (Throwable t) {
                break;
            }
            localLockSuccessList.add(reentrantLock);
        }
        // 逐个获取分布式锁
        for (RLock rLock : serviceLockList) {
            try {
                rLock.lock();
            } catch (Throwable t) {
                break;
            }
            serviceLockSuccessList.add(rLock);
        }
        try {
            return programOrderService.create(programOrderCreateDto);
        } finally {
            // 释放分布式锁，倒序
            for (int i = serviceLockSuccessList.size() - 1; i >= 0; i--) {
                RLock rLock = serviceLockSuccessList.get(i);
                try {
                    rLock.unlock();
                } catch (Throwable t) {
                    log.error("service lock unlock error", t);
                }
            }
            // 释放本地锁，倒序
            for (int i = localLockSuccessList.size() - 1; i >= 0; i--) {
                ReentrantLock reentrantLock = localLockSuccessList.get(i);
                try {
                    reentrantLock.unlock();
                } catch (Throwable t) {
                    log.error("local lock unlock error", t);
                }
            }
        }
    }

    @Override
    public Integer executeOrder() {
        return 2;
    }

    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        ProgramOrderContext.add(ProgramOrderVersion.V2_VERSION.getVersion(), this);
    }
}
