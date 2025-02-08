package com.ticketflow.service.composite.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.dto.SeatDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.service.composite.AbstractProgramCheckHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 订单参数校验
 * @Author: rickey-c
 * @Date: 2025/2/7 21:08
 */
public class ProgramOrderCreateParamCheckHandler extends AbstractProgramCheckHandler {
    @Override
    protected void execute(ProgramOrderCreateDto programOrderCreateDto) {
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        List<Long> ticketUserIdList = programOrderCreateDto.getTicketUserIdList();
        Map<Long, List<Long>> ticketUserIdMap =
                ticketUserIdList.stream().collect(Collectors.groupingBy(ticketUserId -> ticketUserId));
        for (List<Long> value : ticketUserIdMap.values()) {
            // 一个人只能买一张票
            if (value.size() > 1) {
                throw new TicketFlowFrameException(BaseCode.TICKET_USER_ID_REPEAT);
            }
        }
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            // 手动选座校验
            if (seatDtoList.size() != programOrderCreateDto.getTicketUserIdList().size()) {
                // 购票人和座位数量相等
                throw new TicketFlowFrameException(BaseCode.TICKET_USER_COUNT_UNEQUAL_SEAT_COUNT);
            }
            // 参数非空检验
            for (SeatDto seatDto : seatDtoList) {
                if (Objects.isNull(seatDto.getId())) {
                    throw new TicketFlowFrameException(BaseCode.SEAT_ID_EMPTY);
                }
                if (Objects.isNull(seatDto.getTicketCategoryId())) {
                    throw new TicketFlowFrameException(BaseCode.SEAT_TICKET_CATEGORY_ID_EMPTY);
                }
                if (Objects.isNull(seatDto.getRowCode())) {
                    throw new TicketFlowFrameException(BaseCode.SEAT_ROW_CODE_EMPTY);
                }
                if (Objects.isNull(seatDto.getColCode())) {
                    throw new TicketFlowFrameException(BaseCode.SEAT_COL_CODE_EMPTY);
                }
                if (Objects.isNull(seatDto.getPrice())) {
                    throw new TicketFlowFrameException(BaseCode.SEAT_PRICE_EMPTY);
                }
            }
        } else {
            // 自动选座校验
            if (Objects.isNull(programOrderCreateDto.getTicketCategoryId())) {
                throw new TicketFlowFrameException(BaseCode.TICKET_CATEGORY_NOT_EXIST);
            }
            if (Objects.isNull(programOrderCreateDto.getTicketCount())) {
                throw new TicketFlowFrameException(BaseCode.TICKET_COUNT_NOT_EXIST);
            }
            if (programOrderCreateDto.getTicketCount() <= 0) {
                throw new TicketFlowFrameException(BaseCode.TICKET_COUNT_ERROR);
            }
        }

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
