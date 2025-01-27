package com.ticketflow.servicelock.impl;

import com.ticketflow.servicelock.ServiceLocker;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @Description: Redisson读锁
 * @Author: rickey-c
 * @Date: 2025/1/27 16:11
 */
@AllArgsConstructor
public class RedissonReadLocker implements ServiceLocker {
    
    private final RedissonClient redissonClient;

    @Override
    public RLock getLock(String lockKey) {
        return redissonClient.getReadWriteLock(lockKey).readLock();
    }

    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).readLock();
        lock.lock();
        return lock;
    }

    @Override
    public RLock lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).readLock();
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    @Override
    public RLock lock(String lockKey, TimeUnit unit, long leaseTime) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).readLock();
        lock.lock(leaseTime, unit);
        return lock;
    }

    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime) {
        try {
            return redissonClient.getReadWriteLock(lockKey).readLock().tryLock(waitTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) {
        try {
            return redissonClient.getReadWriteLock(lockKey).readLock().tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        redissonClient.getReadWriteLock(lockKey).readLock().unlock();
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }
}
