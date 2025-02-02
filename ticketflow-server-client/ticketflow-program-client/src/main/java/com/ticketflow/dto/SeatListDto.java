package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 节目座位列表 dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "SeatListDto", description = "节目座位列表")
public class SeatListDto {

    @Schema(name = "programId", type = "Long", description = "节目表id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long programId;
}
