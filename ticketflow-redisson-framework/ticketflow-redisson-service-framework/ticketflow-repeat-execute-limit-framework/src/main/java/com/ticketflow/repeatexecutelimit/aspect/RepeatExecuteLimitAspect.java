package com.ticketflow.repeatexecutelimit.aspect;

import com.ticketflow.constant.LockInfoType;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.handler.RedissonDataHandle;
import com.ticketflow.locallock.LocalLockCache;
import com.ticketflow.lockinfo.LockInfoHandle;
import com.ticketflow.lockinfo.factory.LockInfoHandleFactory;
import com.ticketflow.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.ServiceLocker;
import com.ticketflow.servicelock.factory.ServiceLockFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.ticketflow.repeatexecutelimit.constant.RepeatExecuteLimitConstant.PREFIX_NAME;
import static com.ticketflow.repeatexecutelimit.constant.RepeatExecuteLimitConstant.SUCCESS_FLAG;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/27 20:32
 */
@Slf4j
@Order(-11)
@Aspect
@AllArgsConstructor
public class RepeatExecuteLimitAspect {
    
    private final LocalLockCache localLockCache;
    
    private final LockInfoHandleFactory lockInfoHandleFactory;
    
    private final ServiceLockFactory serviceLockFactory;
    
    private final RedissonDataHandle redissonDataHandle;
    
    @Around(value = "@annotation(repeatLimit)")
    public Object aspect(ProceedingJoinPoint joinPoint, RepeatExecuteLimit repeatLimit) throws Throwable {
        Object result;
        //指定保持幂等的时间
        long durationTime = repeatLimit.durationTime();
        //提示信息
        String message = repeatLimit.message();
        Object obj;
        //获取锁信息
        LockInfoHandle lockInfoHandle = lockInfoHandleFactory.
                getLockInfoHandler(LockInfoType.REPEAT_EXECUTE_LIMIT);
        //解析锁名字
        String lockName = lockInfoHandle.getLockName(joinPoint, repeatLimit.name(), repeatLimit.keys());
        //幂等标识
        String repeatFlagName = PREFIX_NAME + lockName;
        //获得幂等标识
        String flagObject = redissonDataHandle.get(repeatFlagName);
        //如果幂等标识的值为success，说明已经有请求在执行了，这次请求直接结束
        if (SUCCESS_FLAG.equals(flagObject)) {
            throw new TicketFlowFrameException(message);
        }
        // 获取本地锁，执行本地锁逻辑
        ReentrantLock localLock = localLockCache.getLock(lockName, false);
        boolean localLockExecuteSuccess = localLock.tryLock();
        if(!localLockExecuteSuccess){
            throw new TicketFlowFrameException(message);
        }
        try {
            // 获取分布式锁，执行分布式锁逻辑
            ServiceLocker serviceLock = serviceLockFactory.getLock(LockType.Reentrant);
            boolean serviceLockExecuteSuccess = serviceLock.tryLock(lockName, TimeUnit.SECONDS, 0);
            if (!serviceLockExecuteSuccess){
                throw new TicketFlowFrameException(message);
            }

            try {
                // 幂等标识
                flagObject = redissonDataHandle.get(repeatFlagName);
                if (SUCCESS_FLAG.equals(flagObject)) {
                    throw new TicketFlowFrameException(message);
                }
                // 执行业务逻辑
                obj = joinPoint.proceed();
                if(durationTime > 0){
                    try{
                        // 设置幂等标识为success
                        redissonDataHandle.set(repeatFlagName,SUCCESS_FLAG,durationTime,TimeUnit.SECONDS);
                    } catch (RuntimeException e) {
                        log.error("getBucket error",e);
                    }
                }
                result = obj;
            } finally {
                serviceLock.unlock(lockName);
            }
        } finally {
            localLock.unlock();
        }
        return result;
    }
    
}
