package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 账户下某个节目的订单数量 dto
 * @Author: rickey-c
 * @Date: 2025/12/3 14:04
 */
@Data
@Schema(title="AccountOrderCountDto", description ="账户下某个节目的订单数量")
public class AccountOrderCountDto {
    
    @Schema(name ="userId", type ="Long", description ="用户id", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Long userId;
    
    @Schema(name ="programId", type ="Long", description ="节目id", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Long programId;
}
