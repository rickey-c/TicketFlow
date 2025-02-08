package com.ticketflow.service.composite;

import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.dto.ProgramRecommendListDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.impl.composite.AbstractComposite;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Description: 节目详情查询验证
 * @Author: rickey-c
 * @Date: 2025/2/8 23:47
 */
@Component
public class ProgramRecommendCheckHandler extends AbstractComposite<ProgramRecommendListDto> {

    @Override
    protected void execute(final ProgramRecommendListDto param) {
        if (Objects.isNull(param.getAreaId()) &&
                Objects.isNull(param.getParentProgramCategoryId()) &&
                Objects.isNull(param.getProgramId())) {
            throw new TicketFlowFrameException(BaseCode.PARAMETERS_CANNOT_BE_EMPTY);
        }
    }

    @Override
    public String type() {
        return CompositeCheckType.PROGRAM_RECOMMEND_CHECK.getValue();
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
