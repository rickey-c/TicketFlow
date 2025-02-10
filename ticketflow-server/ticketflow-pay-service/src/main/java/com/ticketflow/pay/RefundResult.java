package com.ticketflow.pay;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Description: 退款结果
 * @Author: rickey-c
 * @Date: 2025/2/10 11:36
 */
@Data
@AllArgsConstructor
public class RefundResult {
    
    private final boolean success;
    
    private final String body;
    
    private final String message;
}
