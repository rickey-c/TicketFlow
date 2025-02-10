package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 普通规则dto
 * @Author: rickey-c
 * @Date: 2025/1/27 15:33
 */
@Data
@Schema(title = "RuleDto", description = "普通规则")
public class RuleDto {

    @Schema(name = "statTime", type = "Integer", description = "统计时间", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer statTime;

    @Schema(name = "statTimeType", type = "Integer", description = "统计时间类型 1:秒 2:分钟", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer statTimeType;

    @Schema(name = "threshold", type = "Integer", description = "阈值", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer threshold;

    @Schema(name = "effectiveTime", type = "Integer", description = "规则生效限制时间", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer effectiveTime;

    @Schema(name = "effectiveTimeType", type = "Integer", description = "规则生效限制时间类型 1:秒 2:分钟", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer effectiveTimeType;

    private String limitApi;

    @Schema(name = "message", type = "String", description = "提示信息")
    private String message;
}
