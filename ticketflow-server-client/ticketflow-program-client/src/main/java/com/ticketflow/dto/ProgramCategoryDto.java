package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 节目类型dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "ProgramCategoryDto", description = "节目类型")
public class ProgramCategoryDto {

    @Schema(name = "type", type = "Integer", description = "1:一级种类 2:二级种类", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer type;
}
