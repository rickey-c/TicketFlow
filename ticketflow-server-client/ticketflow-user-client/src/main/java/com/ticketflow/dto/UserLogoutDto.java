package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: 用户退出登录dto
 * @Author: rickey-c
 * @Date: 2025/1/31 19:08
 */
@Data
@Schema(title = "UserLogoutDto", description = "用户退出登录")
public class UserLogoutDto {

    @Schema(name = "code", type = "String", description = "渠道code 0001:pc网站", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    @Schema(name = "id", type = "Long", description = "token", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String token;
}