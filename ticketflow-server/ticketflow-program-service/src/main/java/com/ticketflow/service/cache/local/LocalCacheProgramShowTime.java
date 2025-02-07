package com.ticketflow.service.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.ticketflow.entity.ProgramShowTime;
import com.ticketflow.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Description: 节目演出时间本地缓存
 * @Author: rickey-c
 * @Date: 2025/2/7 21:12
 */
@Component
public class LocalCacheProgramShowTime {

    /**
     * 本地缓存
     */
    private Cache<String, ProgramShowTime> localCache;


    /**
     * 本地缓存的容量
     */
    @Value("${maximumSize:10000}")
    private Long maximumSize;

    @PostConstruct
    public void localLockCacheInit() {
        localCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfter(new Expiry<String, ProgramShowTime>() {
                    @Override
                    public long expireAfterCreate(@NonNull final String key, @NonNull final ProgramShowTime value,
                                                  final long currentTime) {
                        return TimeUnit.SECONDS.toNanos(DateUtils.countBetweenSecond(DateUtils.now(), value.getShowTime()));
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull final String key, @NonNull final ProgramShowTime value,
                                                  final long currentTime, @NonNegative final long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(@NonNull final String key, @NonNull final ProgramShowTime value,
                                                final long currentTime, @NonNegative final long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    /**
     * Caffeine的get是线程安全的
     */
    public ProgramShowTime getCache(String id, Function<String, ProgramShowTime> function) {
        return localCache.get(id, function);
    }

    public ProgramShowTime getCache(String id) {
        return localCache.getIfPresent(id);
    }

    public void del(String id) {
        localCache.invalidate(id);
    }
}
