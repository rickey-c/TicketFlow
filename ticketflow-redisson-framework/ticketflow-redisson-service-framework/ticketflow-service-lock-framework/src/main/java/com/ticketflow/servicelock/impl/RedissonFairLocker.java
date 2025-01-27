package com.ticketflow.servicelock.impl;

import com.ticketflow.servicelock.ServiceLocker;
import lombok.AllArgsConstructor;
import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @Description: Redisson公平锁
 * @Author: rickey-c
 * @Date: 2025/1/27 16:00
 */
@AllArgsConstructor
public class RedissonFairLocker implements ServiceLocker {
    
    private final RedissonClient redissonClient;
    
    @Override
    public RLock getLock(String lockKey) {
        return redissonClient.getFairLock(lockKey);
    }

    @Override
    public RLock lock(String lockKey) {
        RLock fairLock = redissonClient.getFairLock(lockKey);
        fairLock.lock();
        return fairLock;
    }

    @Override
    public RLock lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getFairLock(lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    @Override
    public RLock lock(String lockKey, TimeUnit unit, long leaseTime) {
        RLock lock = redissonClient.getFairLock(lockKey);
        lock.lock(leaseTime, unit);
        return lock;
    }

    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime) {
        RLock lock = redissonClient.getFairLock(lockKey);
        try {
            return lock.tryLock(waitTime,unit);
        } catch (InterruptedException e) {
            return false;
        }

    }

    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getFairLock(lockKey);
        try {
            return lock.tryLock(waitTime,leaseTime,unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getFairLock(lockKey);
        lock.unlock();
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }
}
