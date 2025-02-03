package com.ticketflow.lock;

/**
 * @Description: 锁任务
 * @Author: rickey-c
 * @Date: 2025/2/3 20:07
 */
@FunctionalInterface
public interface LockTask<T> {
    /**
     * 执行锁的任务
     *
     * @return 结果
     */
    T execute();
}
