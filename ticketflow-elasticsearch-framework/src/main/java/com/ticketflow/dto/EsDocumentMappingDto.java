package com.ticketflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Description: elasticsearch文档映射
 * @Author: rickey-c
 * @Date: 2025/2/2 20:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsDocumentMappingDto {

    /**
     * 字段名
     */
    private String paramName;

    /**
     * 字段类型
     */
    private String paramType;
}