package com.ticketflow.config;

import com.ticketflow.constant.LockInfoType;
import com.ticketflow.core.ManageLocker;
import com.ticketflow.lockinfo.LockInfoHandle;
import com.ticketflow.lockinfo.factory.LockInfoHandleFactory;
import com.ticketflow.lockinfo.impl.ServiceLockInfoHandle;
import com.ticketflow.servicelock.aspect.ServiceLockAspect;
import com.ticketflow.servicelock.factory.ServiceLockFactory;
import com.ticketflow.util.ServiceLockTool;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/27 17:16
 */
@Configuration
public class ServiceLockAutoConfiguration {

    /**
     * 显示指定Bean名称，返回的是分布式锁
     * @return service-lock
     */
    @Bean(LockInfoType.SERVICE_LOCK)
    public LockInfoHandle serviceLockInfoHandle(){
        return new ServiceLockInfoHandle();
    }
    
    @Bean
    public ManageLocker manageLocker(RedissonClient redissonClient){
        return new ManageLocker(redissonClient);
    }
    
    @Bean
    public ServiceLockFactory serviceLockFactory(ManageLocker manageLocker){
        return new ServiceLockFactory(manageLocker);
    }
    
    @Bean
    public ServiceLockAspect serviceLockAspect(LockInfoHandleFactory lockInfoHandleFactory,
                                               ServiceLockFactory serviceLockFactory){
        return new ServiceLockAspect(lockInfoHandleFactory,serviceLockFactory);
    }
    
    @Bean
    public ServiceLockTool serviceLockTool(LockInfoHandleFactory lockInfoHandleFactory,
                                           ServiceLockFactory serviceLockFactory){
        return new ServiceLockTool(lockInfoHandleFactory,serviceLockFactory);
    }
    
    
}
