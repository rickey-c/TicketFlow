package com.ticketflow.service.cache.local;

import com.ticketflow.utils.DateUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.ticketflow.vo.ProgramVo;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Description: 节目本地缓存
 * @Author: rickey-c
 * @Date: 2025/2/7 20:46
 */
@Component
public class LocalCacheProgram {

    /**
     * 本地缓存
     */
    private Cache<String, ProgramVo> localCache;

    /**
     * 本地缓存的容量
     */
    @Value("${maximumSize:10000}")
    private Long maximumSize;

    @PostConstruct
    public void localLockCacheInit() {
        localCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfter(new Expiry<String, ProgramVo>() {
                    @Override
                    public long expireAfterCreate(@NonNull final String key, @NonNull final ProgramVo value,
                                                  final long currentTime) {
                        return TimeUnit.MILLISECONDS.toNanos(DateUtils.countBetweenSecond(DateUtils.now(), value.getShowTime()));
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull final String key, @NonNull final ProgramVo value,
                                                  final long currentTime, @NonNegative final long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(@NonNull final String key, @NonNull final ProgramVo value,
                                                final long currentTime, @NonNegative final long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    public ProgramVo getCache(String key, Function<String, ProgramVo> function) {
        return localCache.get(key, function);
    }

    public ProgramVo getCache(String key) {
        return localCache.getIfPresent(key);
    }

    public void del(String id) {
        localCache.invalidate(id);
    }


}
