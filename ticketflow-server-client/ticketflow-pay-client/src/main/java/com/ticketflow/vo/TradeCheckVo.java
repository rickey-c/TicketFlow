package com.ticketflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 交易状态查询 vo
 * @Author: rickey-c
 * @Date: 2025/2/9 16:42
 */
@Data
@Schema(title = "TradeCheckVo", description = "交易状态结果")
public class TradeCheckVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "success", type = "boolean", description = "是否成功")
    private boolean success;

    @Schema(name = "payBillStatus", type = "Integer", description = "支付账单状态")
    private Integer payBillStatus;

    @Schema(name = "outTradeNo", type = "String", description = "商户订单号")
    private String outTradeNo;

    @Schema(name = "totalAmount", type = "BigDecimal", description = "支付金额")
    private BigDecimal totalAmount;
}
