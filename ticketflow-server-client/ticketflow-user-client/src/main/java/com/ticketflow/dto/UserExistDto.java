package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 用户手机号dto
 * @Author: rickey-c
 * @Date: 2025/1/31 19:08
 */
@Data
@Schema(title = "UserExistDto", description = "用户是否存在")
public class UserExistDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "mobile", type = "String", description = "手机号", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String mobile;

}
