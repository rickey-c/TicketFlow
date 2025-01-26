package com.ticketflow.toolkit;

/**
 * @Description: Id生成的常量
 * @Author: rickey-c
 * @Date: 2025/1/26 12:53
 */
public class IdGeneratorConstant {
    /**
     * 机器标识位数
     */
    public static final long WORKER_ID_BITS = 5L;
    public static final long DATA_CENTER_ID_BITS = 5L;
    public static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    public static final long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);
}
