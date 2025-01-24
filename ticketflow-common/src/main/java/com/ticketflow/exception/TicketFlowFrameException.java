package com.ticketflow.exception;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.enums.BaseCode;
import lombok.Data;
import lombok.Getter;

/**
 * @Description: 业务异常类
 * @Author: rickey-c
 * @Date: 2025/1/23 16:19
 */
public class TicketFlowFrameException extends BaseException {

    @Getter
    private Integer code;

    @Getter
    private String message;

    public TicketFlowFrameException() {
        super();
    }

    public TicketFlowFrameException(String message) {
        super(message);
    }


    public TicketFlowFrameException(String code, String message) {
        super(message);
        this.code = Integer.parseInt(code);
        this.message = message;
    }

    public TicketFlowFrameException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public TicketFlowFrameException(BaseCode baseCode) {
        super(baseCode.getMsg());
        this.code = baseCode.getCode();
        this.message = baseCode.getMsg();
    }

    public TicketFlowFrameException(ApiResponse apiResponse) {
        super(apiResponse.getMessage());
        this.code = apiResponse.getCode();
        this.message = apiResponse.getMessage();
    }

    public TicketFlowFrameException(Throwable cause) {
        super(cause);
    }

    public TicketFlowFrameException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public TicketFlowFrameException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    
}
