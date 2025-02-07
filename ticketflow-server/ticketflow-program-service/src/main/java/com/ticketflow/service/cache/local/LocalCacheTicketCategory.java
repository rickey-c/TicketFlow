package com.ticketflow.service.cache.local;

import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.vo.TicketCategoryVo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Description: 节目票档本地缓存
 * @Author: rickey-c
 * @Date: 2025/2/7 21:30
 */
@Component
public class LocalCacheTicketCategory {

    /**
     * 本地缓存
     */
    private Cache<Long, List<TicketCategoryVo>> localCache;

    /**
     * 本地缓存的容量
     */
    @Value("${maximumSize:10000}")
    private Long maximumSize;

    @Autowired
    private RedisCache redisCache;

    @PostConstruct
    public void localLockCacheInit() {
        localCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfter(new Expiry<Long, List<TicketCategoryVo>>() {
                    @Override
                    public long expireAfterCreate(@NonNull final Long key,
                                                  @NonNull final List<TicketCategoryVo> value,
                                                  final long currentTime) {
                        // 从redis中获取节目过期时间
                        Long expire = redisCache.getExpire(RedisKeyBuild.createRedisKey
                                (RedisKeyManage.PROGRAM_TICKET_CATEGORY_LIST, key), TimeUnit.MILLISECONDS);
                        return TimeUnit.MILLISECONDS.toNanos(expire);
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull final Long key,
                                                  @NonNull final List<TicketCategoryVo> value,
                                                  final long currentTime,
                                                  @NonNegative final long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(@NonNull final Long key,
                                                @NonNull final List<TicketCategoryVo> value,
                                                final long currentTime,
                                                @NonNegative final long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    /**
     * Caffeine的get是线程安全的
     */
    public List<TicketCategoryVo> getCache(Long id,
                                           Function<Long, List<TicketCategoryVo>> function) {
        return localCache.get(id, function);
    }

    public void del(Long id) {
        localCache.invalidate(id);
    }
}
