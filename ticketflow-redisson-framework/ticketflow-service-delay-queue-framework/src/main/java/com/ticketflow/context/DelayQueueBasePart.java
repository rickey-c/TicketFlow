package com.ticketflow.context;

import com.ticketflow.config.DelayQueueProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.redisson.api.RedissonClient;

/**
 * @Description: 延迟队列基础配置
 * @Author: rickey-c
 * @Date: 2025/2/2 23:32
 */
@Data
@AllArgsConstructor
public class DelayQueueBasePart {

    private final RedissonClient redissonClient;

    private final DelayQueueProperties delayQueueProperties;
}
