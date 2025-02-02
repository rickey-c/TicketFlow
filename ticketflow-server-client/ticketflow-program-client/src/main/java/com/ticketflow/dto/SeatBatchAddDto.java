package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @Description: 节目座位批量添加dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "SeatBatchAddDto", description = "节目座位批量添加")
public class SeatBatchAddDto {

    @Schema(name = "programId", type = "Long", description = "节目表id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long programId;

    @Schema(name = "ticketCategoryId", type = "SeatBatchRelateInfoAddDto", description = "节目座位相关信息", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private List<SeatBatchRelateInfoAddDto> seatBatchRelateInfoAddDtoList;
}
