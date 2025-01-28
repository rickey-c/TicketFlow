package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 普通规则查询dto
 * @Author: rickey-c
 * @Date: 2025/1/28 13:02
 */
@Data
@Schema(title="RuleGetDto", description ="普通规则查询")
public class RuleGetDto {
    
    @Schema(name ="id", type ="String", description ="普通规则id", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Long id;
}
