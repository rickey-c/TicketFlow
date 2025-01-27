package com.ticketflow.core;

/**
 * @Description: 分布式锁-业务名管理
 * @Author: rickey-c
 * @Date: 2025/1/27 16:22
 */
public class DistributedLockConstants {

    /**
     * 	分布式id datacenterId
     * */
    public static final String DATACENTER_ID = "datacenter_id";
    /**
     * api统计定时任务
     * */
    public final static String API_STAT_LOCK = "api_stat_lock";

    /**
     * 分布式锁示例
     * */
    public final static String LOCK_DATA = "lock_data";

    /**
     * 注册用户
     * */
    public final static String REGISTER_USER_LOCK = "t_register_user_lock";

    /**
     * 登录用户
     * */
    public final static String LOGIN_USER_LOCK = "t_login_user_lock";

    /**
     * 节目
     * */
    public final static String PROGRAM_LOCK = "t_program_lock";

    /**
     * 节目分组
     * */
    public final static String PROGRAM_GROUP_LOCK = "t_program_group_lock";

    /**
     * 查看节目
     * */
    public final static String GET_PROGRAM_LOCK = "t_get_program_lock";

    /**
     * 节目演出时间
     * */
    public final static String PROGRAM_SHOW_TIME_LOCK = "t_program_show_time_lock";

    /**
     * 查看节目演出时间
     * */
    public final static String GET_PROGRAM_SHOW_TIME_LOCK = "t_get_program_show_time_lock";

    /**
     * 座位
     * */
    public final static String SEAT_LOCK = "t_seat_lock";

    /**
     * 查看座位
     * */
    public final static String GET_SEAT_LOCK = "t_get_seat_lock";

    /**
     * 票档类型
     * */
    public final static String TICKET_CATEGORY_LOCK = "t_ticket_category_lock";

    /**
     * 查看票档类型
     * */
    public final static String GET_TICKET_CATEGORY_LOCK = "t_get_ticket_category_lock";

    /**
     * 节目类型
     * */
    public final static String PROGRAM_CATEGORY_LOCK = "t_program_category_lock";

    /**
     * 余票数量
     * */
    public final static String REMAIN_NUMBER_LOCK = "t_remain_number_lock";

    /**
     * 查看余票数量
     * */
    public final static String GET_REMAIN_NUMBER_LOCK = "t_get_remain_number_lock";

    /**
     * 取消订单
     * */
    public final static String ORDER_CANCEL_LOCK = "t_order_cancel_lock";


    /**
     * 交易状态检查
     * */
    public final static String TRADE_CHECK = "t_trade_check_lock";

    /**
     * 节目服务订单创建V1
     * */
    public final static String PROGRAM_ORDER_CREATE_V1 = "t_program_order_create_v1_lock";

    /**
     * 节目服务订单创建V2
     * */
    public final static String PROGRAM_ORDER_CREATE_V2 = "t_program_order_create_v2_lock";

    /**
     * 节目服务订单创建V3
     * */
    public final static String PROGRAM_ORDER_CREATE_V3 = "t_program_order_create_v3_lock";

    /**
     * 节目服务订单创建V4
     * */
    public final static String PROGRAM_ORDER_CREATE_V4 = "t_program_order_create_v4_lock";

    /**
     * 支付服务的通用支付
     * */
    public final static String COMMON_PAY = "t_common_pay_lock";

    /**
     * 订单服务的订单支付后状态检查
     * */
    public final static String ORDER_PAY_CHECK = "t_order_pay_check_lock";

    /**
     * 订单服务的订单支付后回调通知
     * */
    public final static String ORDER_PAY_NOTIFY_CHECK = "t_order_pay_notify_check_lock";
}
