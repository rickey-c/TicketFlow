package com.ticketflow.service.composite;

import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.impl.composite.AbstractComposite;

/**
 * @Description: 生成节目订单校验基类
 * @Author: rickey-c
 * @Date: 2025/2/7 21:06
 */
public abstract class AbstractProgramCheckHandler extends AbstractComposite<ProgramOrderCreateDto> {

    @Override
    public String type() {
        return CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue();
    }
}
