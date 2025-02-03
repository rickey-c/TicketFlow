package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 订单退款 dto
 * @Author: rickey-c
 * @Date: 2025/12/3 14:31
 */
@Data
@Schema(title="OrderRefundDto", description ="订单退款")
public class OrderRefundDto {
    
    @Schema(name ="orderNumber", type ="Long", description ="订单编号", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Long orderNumber;
    
}
