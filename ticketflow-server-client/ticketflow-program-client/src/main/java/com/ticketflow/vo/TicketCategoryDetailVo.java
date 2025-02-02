package com.ticketflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: 节目票档详细 vo
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "TicketCategoryDetailVo", description = "节目票档详情")
public class TicketCategoryDetailVo {

    @Schema(name = "programId", type = "Long", description = "节目表id", requiredMode = RequiredMode.REQUIRED)
    private Long programId;

    @Schema(name = "introduce", type = "String", description = "介绍", requiredMode = RequiredMode.REQUIRED)
    private String introduce;

    @Schema(name = "price", type = "BigDecimal", description = "价格", requiredMode = RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(name = "totalNumber", type = "Long", description = "总数量", requiredMode = RequiredMode.REQUIRED)
    private Long totalNumber;

    @Schema(name = "remainNumber", type = "Long", description = "剩余数量", requiredMode = RequiredMode.REQUIRED)
    private Long remainNumber;


}
