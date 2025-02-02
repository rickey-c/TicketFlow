package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 节目票档 dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "TicketCategoryDto", description = "节目票档")
public class TicketCategoryDto {

    @Schema(name = "id", type = "Long", description = "id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long id;

}
