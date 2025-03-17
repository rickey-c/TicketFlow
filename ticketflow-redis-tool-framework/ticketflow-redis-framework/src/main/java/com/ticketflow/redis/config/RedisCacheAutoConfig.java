package com.ticketflow.redis.config;

import com.ticketflow.redis.RedisCacheImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/25 13:31
 */
@AutoConfiguration
public class RedisCacheAutoConfig {

    @Bean
    public RedisCacheImpl redisCache(@Qualifier("redisToolStringRedisTemplate") StringRedisTemplate stringRedisTemplate) {
        return new RedisCacheImpl(stringRedisTemplate);
    }
}
