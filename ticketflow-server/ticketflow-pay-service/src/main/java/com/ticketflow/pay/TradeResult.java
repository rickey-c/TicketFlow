package com.ticketflow.pay;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: 支付状态查询
 * @Author: rickey-c
 * @Date: 2025/2/10 11:36
 */
@Data
public class TradeResult {
    
    private boolean success;
    
    private Integer payBillStatus;
    
    private String outTradeNo;
    
    private BigDecimal totalAmount;
}
