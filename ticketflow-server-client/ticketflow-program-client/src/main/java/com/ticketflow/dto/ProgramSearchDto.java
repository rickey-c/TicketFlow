package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 节目搜索dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "ProgramSearchDto", description = "节目搜索")
public class ProgramSearchDto extends ProgramPageListDto {

    @Schema(name = "content", type = "String", description = "搜索内容")
    private String content;
}
