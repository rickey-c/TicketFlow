package com.ticketflow.locallock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 本地锁
 * @Author: rickey-c
 * @Date: 2025/1/26 14:33
 */
public class LocalLockCache {

    /**
     * 本地锁缓存
     */
    private Cache<String, ReentrantLock> localLockCache;

    /**
     * 本地锁过期时间
     */
    @Value("${durationTime:48}")
    private Integer durationTime;

    @PostConstruct
    public void localLockCacheInit() {
        localLockCache = Caffeine.newBuilder()
                .expireAfterWrite(durationTime, TimeUnit.HOURS)
                .build();
    }

    public ReentrantLock getLock(String lockKey, boolean fair) {
        return localLockCache.get(lockKey, key -> new ReentrantLock(fair));
    }

}
