package com.ticketflow.service.composite.impl;

import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.BusinessStatus;
import com.ticketflow.dto.ProgramGetDto;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.service.ProgramService;
import com.ticketflow.service.composite.AbstractProgramCheckHandler;
import com.ticketflow.vo.ProgramVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Description: 节目检查
 * @Author: rickey-c
 * @Date: 2025/2/9 00:08
 */
@Component
public class ProgramDetailCheckHandler extends AbstractProgramCheckHandler {

    @Autowired
    private ProgramService programService;

    @Override
    protected void execute(ProgramOrderCreateDto programOrderCreateDto) {
        ProgramGetDto programGetDto = new ProgramGetDto();
        programGetDto.setId(programOrderCreateDto.getProgramId());
        ProgramVo programVo = programService.detail(programGetDto);
        // 不允许手动选座，且传入座位，要抛出异常
        if (programVo.getPermitChooseSeat().equals(BusinessStatus.NO.getCode())) {
            if (Objects.nonNull(programOrderCreateDto.getSeatDtoList())) {
                throw new TicketFlowFrameException(BaseCode.PROGRAM_NOT_ALLOW_CHOOSE_SEAT);
            }
        }
        // 针对手动选座的座位数量
        Integer seatCount = Optional.ofNullable(programOrderCreateDto.getSeatDtoList()).map(List::size).orElse(0);
        // 针对自动选座的票档数量
        Integer ticketCount = Optional.ofNullable(programOrderCreateDto.getTicketCount()).orElse(0);
        // 购票数量超出每笔订单限制，抛异常
        if (seatCount > programVo.getPerOrderLimitPurchaseCount() || ticketCount > programVo.getPerOrderLimitPurchaseCount()) {
            throw new TicketFlowFrameException(BaseCode.PER_ORDER_PURCHASE_COUNT_OVER_LIMIT);
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
