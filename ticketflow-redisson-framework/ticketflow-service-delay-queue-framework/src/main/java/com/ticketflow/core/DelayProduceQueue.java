package com.ticketflow.core;

import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @Description: 生产者 延迟队列
 * @Author: rickey-c
 * @Date: 2025/2/2 23:37
 */
public class DelayProduceQueue extends DelayBaseQueue {

    private final RDelayedQueue<String> delayedQueue;

    public DelayProduceQueue(RedissonClient redissonClient, String relTopic) {
        super(redissonClient, relTopic);
        this.delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
    }

    public void offer(String content, long delayTime, TimeUnit timeUnit) {
        delayedQueue.offer(content, delayTime, timeUnit);
    }
}
