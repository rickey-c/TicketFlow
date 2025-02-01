package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 购票人dto
 * @Author: rickey-c
 * @Date: 2025/1/31 19:08
 */
@Data
@Schema(title = "TicketUserDto", description = "购票人")
public class TicketUserDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(name = "userId", type = "Long", description = "用户id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long userId;

    @Schema(name = "relName", type = "String", description = "用户真实名字", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String relName;

    @Schema(name = "idType", type = "Integer",
            description = "证件类型 1:身份证 2:港澳台居民居住证 3:港澳居民来往内地通行证 4:台湾居民来往内地通行证 5:护照 6:外国人永久居住证"
            , requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer idType;

    @Schema(name = "idNumber", type = "String", description = "证件号码", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String idNumber;
}
