package com.ticketflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 校验是否需要验证码
 * @Author: rickey-c
 * @Date: 2025/2/1 16:34
 */
@AllArgsConstructor
public enum VerifyCaptcha {

    NO(0, "no", "不需要"),

    YES(1, "yes", "需要"),
    ;

    @Getter
    private Integer code;

    @Getter
    private String value;

    @Getter
    private String msg;


}
