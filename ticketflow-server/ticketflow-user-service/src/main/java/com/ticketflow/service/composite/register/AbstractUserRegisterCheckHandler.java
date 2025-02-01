package com.ticketflow.service.composite.register;

import com.ticketflow.dto.UserRegisterDto;
import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.impl.composite.AbstractComposite;

/**
 * @Description: 用户注册校验基类
 * @Author: rickey-c
 * @Date: 2025/2/1 16:25
 */
public abstract class AbstractUserRegisterCheckHandler extends AbstractComposite<UserRegisterDto> {

    @Override
    public String type() {
        return CompositeCheckType.USER_REGISTER_CHECK.getValue();
    }

}
