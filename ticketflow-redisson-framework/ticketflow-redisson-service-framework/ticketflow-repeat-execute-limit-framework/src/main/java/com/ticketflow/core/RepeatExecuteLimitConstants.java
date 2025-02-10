package com.ticketflow.core;

/**
 * @Description: 幂等锁常量
 * @Author: rickey-c
 * @Date: 2025/1/27 20:20
 */
public class RepeatExecuteLimitConstants {

    public static final String CONSUMER_API_DATA_MESSAGE = "consumer_api_data_message";

    public static final String CREATE_PROGRAM_ORDER = "create_program_order";

    public final static String CANCEL_PROGRAM_ORDER = "cancel_program_order";

    public static final String CREATE_PROGRAM_ORDER_MQ = "create_program_order_mq";

    public static final String PROGRAM_CACHE_REVERSE_MQ = "program_cache_reverse_mq";

}
