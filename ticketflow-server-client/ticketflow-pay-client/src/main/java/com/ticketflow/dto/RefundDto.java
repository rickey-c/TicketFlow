package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: 退款 dto
 * @Author: rickey-c
 * @Date: 2025/2/9 16:41
 */
@Data
@Schema(title = "RefundDto", description = "退款")
public class RefundDto {

    @Schema(name = "orderNumber", type = "Long", description = "订单号", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String orderNumber;

    @Schema(name = "amount", type = "BigDecimal", description = "退款金额", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private BigDecimal amount;

    @Schema(name = "channel", type = "Integer", description = "退款渠道 alipay：支付宝 wx：微信", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private String channel;

    @Schema(name = "reason", type = "String", description = "退款原因")
    private String reason;
}
