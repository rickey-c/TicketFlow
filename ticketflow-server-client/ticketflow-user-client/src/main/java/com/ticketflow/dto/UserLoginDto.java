package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: 用户登录 dto
 * @Author: rickey-c
 * @Date: 2025/1/31 19:08
 */
@Data
@Schema(title = "UserLoginDto", description = "用户登录")
public class UserLoginDto {

    @Schema(name = "code", type = "String", description = "渠道code 0001:pc网站", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    @Schema(name = "name", type = "String", description = "用户手机号")
    private String mobile;

    @Schema(name = "email", type = "String", description = "用户邮箱")
    private String email;

    @Schema(name = "password", type = "String", description = "密码", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String password;
}