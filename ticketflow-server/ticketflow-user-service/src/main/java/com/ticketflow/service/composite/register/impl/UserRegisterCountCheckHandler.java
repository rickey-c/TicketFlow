package com.ticketflow.service.composite.register.impl;

import com.ticketflow.dto.UserRegisterDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.service.composite.register.AbstractUserRegisterCheckHandler;
import com.ticketflow.service.tool.RequestCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 注册限流校验
 * @Author: rickey-c
 * @Date: 2025/2/1 16:32
 */
@Component
public class UserRegisterCountCheckHandler extends AbstractUserRegisterCheckHandler {

    @Autowired
    private RequestCounter requestCounter;

    @Override
    protected void execute(final UserRegisterDto param) {
        boolean result = requestCounter.onRequest();
        if (result) {
            throw new TicketFlowFrameException(BaseCode.USER_REGISTER_FREQUENCY);
        }
    }

    @Override
    public Integer executeParentOrder() {
        return 1;
    }

    @Override
    public Integer executeTier() {
        return 2;
    }

    @Override
    public Integer executeOrder() {
        return 1;
    }
}
