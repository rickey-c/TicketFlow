package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 用户密码更新dto
 * @Author: rickey-c
 * @Date: 2025/1/31 19:08
 */
@Data
@Schema(title = "UserUpdatePasswordDto", description = "修改用户密码")
public class UserUpdatePasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", type = "Long", description = "用户id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(name = "password", type = "String", description = "密码", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String password;

}
