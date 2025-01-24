package com.ticketflow.exception;

import java.util.List;

/**
 * @Description: 参数错误异常类
 * @Author: rickey-c
 * @Date: 2025/1/23 16:17
 */
public class ArgumentException extends BaseException {

    private Integer code;
    private List<ArgumentError> argumentErrorList;

    public ArgumentException(Integer code, List<ArgumentError> argumentErrorList) {
        this.code = code;
        this.argumentErrorList = argumentErrorList;
    }

    public ArgumentException(String message) {
        super(message);
    }

    public ArgumentException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ArgumentException(Throwable cause) {
        super(cause);
    }

    public ArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
