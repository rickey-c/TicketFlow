package com.ticketflow.pay;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Description: 支付结果
 * @Author: rickey-c
 * @Date: 2025/2/10 11:36
 */
@Data
@AllArgsConstructor
public class PayResult {

    private final boolean success;

    private final String body;
}
