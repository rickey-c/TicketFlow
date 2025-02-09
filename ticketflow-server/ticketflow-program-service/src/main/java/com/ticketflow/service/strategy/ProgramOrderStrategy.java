package com.ticketflow.service.strategy;


import com.ticketflow.dto.ProgramOrderCreateDto;

/**
 * @Description: 节目订单
 * @Author: rickey-c
 * @Date: 2025/2/9 15:46
 */
public interface ProgramOrderStrategy {

    /**
     * 创建订单
     *
     * @param programOrderCreateDto 订单参数
     * @return 订单编号
     */
    String createOrder(ProgramOrderCreateDto programOrderCreateDto);
}
