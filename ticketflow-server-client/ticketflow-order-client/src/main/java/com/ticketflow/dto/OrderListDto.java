package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 订单列表查询 dto
 * @Author: rickey-c
 * @Date: 2025/12/3 14:20
 */
@Data
@Schema(title="OrderListDto", description ="订单列表查询")
public class OrderListDto {
    
    @Schema(name ="userId", type ="Long", description ="用户id", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Long userId;
    
}
