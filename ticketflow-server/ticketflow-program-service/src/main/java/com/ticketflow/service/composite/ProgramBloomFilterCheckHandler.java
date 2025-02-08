package com.ticketflow.service.composite;

import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.dto.ProgramGetDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.handler.BloomFilterHandler;
import com.ticketflow.impl.composite.AbstractComposite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 节目详情查询验证
 * @Author: rickey-c
 * @Date: 2025/2/8 23:57
 */
@Component
public class ProgramBloomFilterCheckHandler extends AbstractComposite<ProgramGetDto> {

    @Autowired
    private BloomFilterHandler bloomFilterHandler;

    @Override
    protected void execute(ProgramGetDto param) {
        boolean contains = bloomFilterHandler.contains(String.valueOf(param.getId()));
        if (!contains) {
            throw new TicketFlowFrameException(BaseCode.PROGRAM_NOT_EXIST);
        }
    }

    @Override
    public String type() {
        return CompositeCheckType.PROGRAM_DETAIL_CHECK.getValue();
    }

    @Override
    public Integer executeParentOrder() {
        return 0;
    }

    @Override
    public Integer executeTier() {
        return 1;
    }

    @Override
    public Integer executeOrder() {
        return 1;
    }
}
