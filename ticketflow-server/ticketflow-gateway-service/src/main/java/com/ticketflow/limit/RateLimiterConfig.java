package com.ticketflow.limit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 限流配置类
 * @Author: rickey-c
 * @Date: 2025/1/30 13:49
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiterProperty rateLimiterProperty() {
        return new RateLimiterProperty();
    }

    @Bean
    public RateLimiter rateLimiter(RateLimiterProperty rateLimiterProperty) {
        return new RateLimiter(rateLimiterProperty.getRatePermits());
    }
}
