package com.ticketflow.core;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;

/**
 * @Description: 延迟队列，阻塞队列
 * @Author: rickey-c
 * @Date: 2025/2/2 23:35
 */
@Slf4j
public class DelayBaseQueue {

    protected final RedissonClient redissonClient;

    protected final RBlockingQueue<String> blockingQueue;

    public DelayBaseQueue(RedissonClient redissonClient, String relTopic) {
        this.redissonClient = redissonClient;
        this.blockingQueue = redissonClient.getBlockingQueue(relTopic);
    }
}
