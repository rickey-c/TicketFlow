package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @Description: 地区列表查询Dto
 * @Author: rickey-c
 * @Date: 2025/1/24 15:45
 */
@Data
@Schema(title = "AreaSelectDto", description = "AreaSelectDto")
public class AreaSelectDto {

    @Schema(name = "idList", type = "List<Long>", description = "id集合", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private List<Long> idList;
}
