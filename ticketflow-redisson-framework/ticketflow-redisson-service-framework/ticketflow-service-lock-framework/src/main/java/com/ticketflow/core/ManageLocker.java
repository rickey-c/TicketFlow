package com.ticketflow.core;

import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.ServiceLocker;
import com.ticketflow.servicelock.impl.RedissonFairLocker;
import com.ticketflow.servicelock.impl.RedissonReadLocker;
import com.ticketflow.servicelock.impl.RedissonReentrantLocker;
import com.ticketflow.servicelock.impl.RedissonWriteLocker;
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
public class ManageLocker {

    private final Map<LockType, ServiceLocker> cacheLocker = new HashMap<>();

    public ManageLocker(RedissonClient redissonClient) {
        cacheLocker.put(Reentrant, new RedissonReentrantLocker(redissonClient));
        cacheLocker.put(Fair, new RedissonFairLocker(redissonClient));
        cacheLocker.put(Write, new RedissonWriteLocker(redissonClient));
        cacheLocker.put(Read, new RedissonReadLocker(redissonClient));
    }

    public ServiceLocker getReentrantLocker() {
        return cacheLocker.get(Reentrant);
    }

    public ServiceLocker getFairLocker() {
        return cacheLocker.get(Fair);
    }

    public ServiceLocker getWriteLocker() {
        return cacheLocker.get(Write);
    }

    public ServiceLocker getReadLocker() {
        return cacheLocker.get(Read);
    }
}
