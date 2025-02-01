package com.ticketflow.service.composite.register.impl;

import com.ticketflow.dto.UserRegisterDto;
import com.ticketflow.service.UserService;
import com.ticketflow.service.composite.register.AbstractUserRegisterCheckHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 用户存在校验
 * @Author: rickey-c
 * @Date: 2025/2/1 16:27
 */
@Component
public class UserExistCheckHandler extends AbstractUserRegisterCheckHandler {

    @Autowired
    private UserService userService;

    @Override
    protected void execute(UserRegisterDto userRegisterDto) {
        userService.doExist(userRegisterDto.getMobile());
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
        return 2;
    }
}
