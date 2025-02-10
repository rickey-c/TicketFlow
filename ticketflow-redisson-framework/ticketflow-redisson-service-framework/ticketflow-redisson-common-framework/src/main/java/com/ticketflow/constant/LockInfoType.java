package com.ticketflow.constant;

/**
 * @Description: 锁类型
 * @Author: rickey-c
 * @Date: 2025/1/26 14:17
 */
public class LockInfoType {

    /**
     * 幂等锁
     */
    public static final String REPEAT_EXECUTE_LIMIT = "repeat_execute_limit";

    /**
     * 分布式锁
     */
    public static final String SERVICE_LOCK = "service_lock";

}
