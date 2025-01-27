package com.ticketflow.config;

import com.ticketflow.constant.LockInfoType;
import com.ticketflow.handler.RedissonDataHandle;
import com.ticketflow.locallock.LocalLockCache;
import com.ticketflow.lockinfo.LockInfoHandle;
import com.ticketflow.lockinfo.factory.LockInfoHandleFactory;
import com.ticketflow.lockinfo.impl.RepeatExecuteLimitLockInfoHandle;
import com.ticketflow.repeatexecutelimit.aspect.RepeatExecuteLimitAspect;
import com.ticketflow.servicelock.factory.ServiceLockFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 幂等锁自动配置类
 * @Author: rickey-c
 * @Date: 2025/1/27 20:52
 */
@Configuration
public class RepeatExecuteLimitAutoConfiguration {

    @Bean(LockInfoType.REPEAT_EXECUTE_LIMIT)
    public LockInfoHandle repeatExecuteLimitHandle() {
        return new RepeatExecuteLimitLockInfoHandle();
    }

    @Bean
    public RepeatExecuteLimitAspect repeatExecuteLimitAspect(LocalLockCache localLockCache,
                                                             LockInfoHandleFactory lockInfoHandleFactory,
                                                             ServiceLockFactory serviceLockFactory,
                                                             RedissonDataHandle redissonDataHandle) {
        return new RepeatExecuteLimitAspect(localLockCache, lockInfoHandleFactory, serviceLockFactory, redissonDataHandle);
    }
}
