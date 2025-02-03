package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 订单支付后的状态检查 dto
 * @Author: rickey-c
 * @Date: 2025/12/3 14:40
 */
@Data
@Schema(title="OrderPayCheckDto", description ="订单支付后状态检查")
public class OrderPayCheckDto {
    
    @Schema(name ="orderNumber", type ="String", description ="订单编号", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Long orderNumber;
    
    @Schema(name ="payChannelType", type ="Integer", description ="支付方式1.支付宝 2.微信", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Integer payChannelType;
}
