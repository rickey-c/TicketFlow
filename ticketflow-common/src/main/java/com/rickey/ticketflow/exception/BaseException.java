package com.rickey.ticketflow.exception;

/**
 * @Description: 基础异常类
 * @Author: rickey-c
 * @Date: 2025/1/23 16:16
 */
public class BaseException extends RuntimeException {
    public BaseException() {

    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
    }
}
