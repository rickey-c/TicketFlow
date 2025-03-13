package com.ticketflow.service.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.ticketflow.locallock.LocalLockCache;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.dto.SeatDto;
import com.ticketflow.lock.LockTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Description: 节目订单
 * @Author: rickey-c
 * @Date: 2025/2/9 15:19
 */
@Slf4j
@Component
public class BaseProgramOrder {

    @Autowired
    private LocalLockCache localLockCache;

    public String localLockCreateOrder(String lockKeyPrefix, ProgramOrderCreateDto programOrderCreateDto,
                                       LockTask<String> lockTask) {
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        List<Long> ticketCategoryIdList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            ticketCategoryIdList =
                    seatDtoList.stream().map(SeatDto::getTicketCategoryId).distinct().collect(Collectors.toList());
        } else {
            ticketCategoryIdList.add(programOrderCreateDto.getTicketCategoryId());
        }
        List<ReentrantLock> localLockList = new ArrayList<>(ticketCategoryIdList.size());
        List<ReentrantLock> localLockSuccessList = new ArrayList<>(ticketCategoryIdList.size());
        for (Long ticketCategoryId : ticketCategoryIdList) {
            String lockKey = StrUtil.join("-", lockKeyPrefix,
                    programOrderCreateDto.getProgramId(), ticketCategoryId);
            ReentrantLock localLock = localLockCache.getLock(lockKey, false);
            localLockList.add(localLock);
        }
        for (ReentrantLock reentrantLock : localLockList) {
            try {
                reentrantLock.lock();
            } catch (Throwable t) {
                break;
            }
            localLockSuccessList.add(reentrantLock);
        }
        try {
            // 函数式接口执行
            return lockTask.execute();
        } finally {
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

}
