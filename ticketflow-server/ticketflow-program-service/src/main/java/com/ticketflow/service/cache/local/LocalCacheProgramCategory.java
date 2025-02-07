package com.ticketflow.service.cache.local;

import com.ticketflow.entity.ProgramCategory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.function.Function;

/**
 * @Description: 节目类型本地缓存
 * @Author: rickey-c
 * @Date: 2025/2/7 20:59
 */
@Component
public class LocalCacheProgramCategory {

    /**
     * 本地缓存
     */
    private Cache<String, ProgramCategory> localCache;

    @PostConstruct
    public void localLockCacheInit() {
        localCache = Caffeine.newBuilder().build();
    }

    /**
     * 获得锁，Caffeine的get是线程安全的
     */
    public ProgramCategory get(String id, Function<String, ProgramCategory> function) {
        return localCache.get(id, function);
    }
}