package com.ticketflow.util;

/**
 * @Description: 分布式锁有返回值的业务方法
 * @Author: rickey-c
 * @Date: 2025/1/27 17:00
 */
@FunctionalInterface
public interface TaskCall<T> {

    /**
     * call
     *
     * @return
     */
    T call();

}
