package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 用户实名认证dto
 * @Author: rickey-c
 * @Date: 2025/1/31 19:08
 */
@Data
@Schema(title = "UserAuthenticationDto", description = "用户实名认证")
public class UserAuthenticationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", type = "Long", description = "用户id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(name = "relName", type = "String", description = "用户真实名字", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String relName;

    @Schema(name = "idNumber", type = "String", description = "身份证号码", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String idNumber;

}
