package com.rickey.ticketflow.exception;

import lombok.Data;

/**
 * @Description: 参数错误
 * @Author: rickey-c
 * @Date: 2025/1/23 16:15
 */
@Data
public class ArgumentError {

    private String argumentName;

    private String message;
}
