package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 节目失效dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "ProgramInvalidDto", description = "节目失效")
public class ProgramInvalidDto {

    @Schema(name = "id", type = "Long", description = "id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long id;
}
