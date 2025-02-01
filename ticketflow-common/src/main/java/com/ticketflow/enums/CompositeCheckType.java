package com.ticketflow.enums;

/**
 * @Description: 组合模式类型
 * @Author: rickey-c
 * @Date: 2025/1/31 23:33
 */
public enum CompositeCheckType {
    /**
     * 组合模式类型
     * */
    USER_REGISTER_CHECK(1,"user_register_check","用户注册"),

    /**
     * 节目详情查看
     * */
    PROGRAM_DETAIL_CHECK(2,"program_detail_check","节目详情"),

    /**
     * 订单创建
     * */
    PROGRAM_ORDER_CREATE_CHECK(3,"program_order_create_check","订单创建"),

    /**
     * 节目推荐
     * */
    PROGRAM_RECOMMEND_CHECK(4,"program_recommend_check","节目推荐"),


    ;

    private Integer code;

    private String value;

    private String msg;

    CompositeCheckType(Integer code, String value, String msg) {
        this.code = code;
        this.value = value;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg == null ? "" : this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public static String getMsg(Integer code) {
        for (CompositeCheckType re : CompositeCheckType.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re.msg;
            }
        }
        return "";
    }

    public static CompositeCheckType getRc(Integer code) {
        for (CompositeCheckType re : CompositeCheckType.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re;
            }
        }
        return null;
    }
}

