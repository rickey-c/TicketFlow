package com.ticketflow.config;

import com.ticketflow.context.DelayQueueBasePart;
import com.ticketflow.context.DelayQueueContext;
import com.ticketflow.event.DelayQueueInitHandler;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 延迟队列配置
 * @Author: rickey-c
 * @Date: 2025/2/3 0:19
 */
@Configuration
@EnableConfigurationProperties(DelayQueueProperties.class)
public class DelayQueueAutoConfig {

    @Bean
    public DelayQueueInitHandler delayQueueInitHandler(DelayQueueBasePart delayQueueBasePart) {
        return new DelayQueueInitHandler(delayQueueBasePart);
    }

    @Bean
    public DelayQueueBasePart delayQueueBasePart(RedissonClient redissonClient, DelayQueueProperties delayQueueProperties) {
        return new DelayQueueBasePart(redissonClient, delayQueueProperties);
    }

    @Bean
    public DelayQueueContext delayQueueContext(DelayQueueBasePart delayQueueBasePart) {
        return new DelayQueueContext(delayQueueBasePart);
    }
}
