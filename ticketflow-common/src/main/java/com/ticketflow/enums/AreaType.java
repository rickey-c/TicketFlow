package com.ticketflow.enums;

import lombok.Getter;

/**
 * @Description: 地区类型枚举
 * @Author: rickey-c
 * @Date: 2025/1/27 21:18
 */
public enum AreaType {
    /**
     * 省
     * */
    PROVINCE(1,"省"),
    /**
     * 市
     * */
    MUNICIPALITIES(2,"市"),
    
    /**
     * 区或县
     * */
    PREFECTURE(3,"区或县"),
    ;

    @Getter
    private Integer code;

    private String msg;

    AreaType(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
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

    public static String getMsg(Integer code) {
        for (AreaType re : AreaType.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re.msg;
            }
        }
        return "";
    }

    public static AreaType getRc(Integer code) {
        for (AreaType re : AreaType.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re;
            }
        }
        return null;
    }
}
