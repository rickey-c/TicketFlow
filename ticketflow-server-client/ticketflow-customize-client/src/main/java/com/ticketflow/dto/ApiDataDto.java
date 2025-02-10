package com.ticketflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: api调用数据dto
 * @Author: rickey-c
 * @Date: 2025/1/25 10:12
 */
@Data
@Schema(title = "ApiDataDto", description = "api被限制调用记录")
public class ApiDataDto {

    @Schema(name = "pageNo", type = "Integer", description = "页码", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer pageNo;

    @Schema(name = "pageSize", type = "Integer", description = "页数", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private Integer pageSize;

    @Schema(name = "startDate", type = "String", description = "开始日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String startDate;

    @Schema(name = "endDate", type = "String", description = "结束日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endDate;

    @Schema(name = "apiAddress", type = "String", description = "api的ip地址")
    private String apiAddress;

    @Schema(name = "apiUrl", type = "String", description = "api路径")
    private String apiUrl;
}
