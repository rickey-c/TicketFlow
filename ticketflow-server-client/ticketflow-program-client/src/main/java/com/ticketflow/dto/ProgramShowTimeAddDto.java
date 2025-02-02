package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 节目演出时间添加 dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "ProgramShowTimeAddDto", description = "节目演出时间添加")
public class ProgramShowTimeAddDto {

    @Schema(name = "programId", type = "Long", description = "节目表id", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Long programId;

    @Schema(name = "showTime", type = "Date", description = "演出时间", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Date showTime;

    @Schema(name = "showDayTime", type = "Date", description = "演出时间(精确到天)", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Date showDayTime;

    @Schema(name = "showWeekTime", type = "String", description = "演出时间所在的星期", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String showWeekTime;
}
