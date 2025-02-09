package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 支付 dto
 * @Author: rickey-c
 * @Date: 2025/2/9 16:35
 */
@Data
@Schema(title = "PayDto", description = "支付")
public class PayDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(name = "platform", type = "Integer", description = "支付平台 1：小程序  2：H5  3：pc网页  4：app", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer platform;

    @Schema(name = "orderNumber", type = "Long", description = "订单号", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String orderNumber;

    @Schema(name = "subject", type = "String", description = "订单标题", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String subject;

    @Schema(name = "price", type = "BigDecimal", description = "价格", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private BigDecimal price;

    @Schema(name = "channel", type = "Integer", description = "支付渠道 alipay：支付宝 wx：微信", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private String channel;

    @Schema(name = "payBillType", type = "Integer", description = "支付种类", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer payBillType;

    @Schema(name = "notifyUrl", type = "String", description = "支付成功后通知接口地址", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String notifyUrl;

    @Schema(name = "returnUrl", type = "String", description = "支付成功后跳转页面", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String returnUrl;
}
