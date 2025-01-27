package com.ticketflow.servicelock;

/**
 * @Description: 锁类型
 * @Author: rickey-c
 * @Date: 2025/1/27 15:55
 */
public enum LockType {
    /**
     * 可重入锁
     */
    Reentrant,
    /**
     * 公平锁
     */
    Fair,
    /**
     * 读锁
     */
    Read,
    /**
     * 写锁
     */
    Write;

    LockType() {
    }

}

