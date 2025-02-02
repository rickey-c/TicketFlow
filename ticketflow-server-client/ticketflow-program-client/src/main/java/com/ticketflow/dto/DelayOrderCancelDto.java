package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 延迟订单取消dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "DelayOrderCancelDto", description = "延迟订单取消")
public class DelayOrderCancelDto {

    @Schema(name = "orderNumber", type = "Long", description = "订单编号", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long orderNumber;
}
