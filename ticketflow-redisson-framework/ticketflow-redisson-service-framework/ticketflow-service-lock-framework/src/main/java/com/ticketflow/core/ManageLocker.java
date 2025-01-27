package com.ticketflow.core;

import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.ServiceLocker;
import com.ticketflow.servicelock.impl.RedissonFairLocker;
import com.ticketflow.servicelock.impl.RedissonReadLocker;
import com.ticketflow.servicelock.impl.RedissonReentrantLocker;
import com.ticketflow.servicelock.impl.RedissonWriteLocker;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

import static com.ticketflow.servicelock.LockType.Reentrant;
import static com.ticketflow.servicelock.LockType.Fair;
import static com.ticketflow.servicelock.LockType.Read;
import static com.ticketflow.servicelock.LockType.Write;

/**
 * @Description: 分布式锁缓存
 * @Author: rickey-c
 * @Date: 2025/1/27 16:22
 */
@AllArgsConstructor
public class ManageLocker {
    
    private final Map<LockType, ServiceLocker> lockerMap = new HashMap<>();

    public ManageLocker(RedissonClient redissonClient){
        lockerMap.put(Reentrant,new RedissonReentrantLocker(redissonClient));
        lockerMap.put(Fair,new RedissonFairLocker(redissonClient));
        lockerMap.put(Write,new RedissonWriteLocker(redissonClient));
        lockerMap.put(Read,new RedissonReadLocker(redissonClient));
    }

    public ServiceLocker getReentrantLocker(){
        return lockerMap.get(Reentrant);
    }

    public ServiceLocker getFairLocker(){
        return lockerMap.get(Fair);
    }

    public ServiceLocker getWriteLocker(){
        return lockerMap.get(Write);
    }

    public ServiceLocker getReadLocker(){
        return lockerMap.get(Read);
    }
}
