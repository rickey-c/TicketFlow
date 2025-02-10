package com.ticketflow.servicelock.info;

/**
 * @Description: 锁超时处理器
 * @Author: rickey-c
 * @Date: 2025/1/27 15:57
 */
public interface LockTimeOutHandler {

    /**
     * 超时处理
     *
     * @param lockName
     */
    void handler(String lockName);
}
