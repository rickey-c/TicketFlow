package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: test dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
public class TestSendDto {

    private Long count;

    @Schema(name = "message", type = "String", description = "消息", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String message;

    private Long time;
}
