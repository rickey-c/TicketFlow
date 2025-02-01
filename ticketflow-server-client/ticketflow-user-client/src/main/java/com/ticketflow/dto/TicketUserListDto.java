package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 购票人列表查询
 * @Author: rickey-c
 * @Date: 2025/1/31 19:08
 */
@Data
@Schema(title = "TicketUserListDto", description = "购票人列表入参")
public class TicketUserListDto {

    @Schema(name = "userId", type = "Long", description = "用户id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long userId;
}
