package com.ticketflow.enums;

/**
 * @Description: 销售状态枚举
 * @Author: rickey-c
 * @Date: 2025/2/9 14:18
 */
public enum SellStatus {
    /**
     * 售卖状态
     */
    NO_SOLD(1, "未售卖"),
    LOCK(2, "锁定"),
    SOLD(3, "已售卖"),
    ;

    private Integer code;

    private String msg;

    SellStatus(Integer code, String msg) {
        this.code = code;
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

    public static String getMsg(Integer code) {
        for (SellStatus re : SellStatus.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re.msg;
            }
        }
        return "";
    }

    public static SellStatus getRc(Integer code) {
        for (SellStatus re : SellStatus.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re;
            }
        }
        return null;
    }
}
