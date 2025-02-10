package com.ticketflow.util;

/**
 * @Description: 分布式锁没有返回值的业务方法
 * @Author: rickey-c
 * @Date: 2025/1/27 17:02
 */
@FunctionalInterface
public interface TaskRun {

    void run();

}
