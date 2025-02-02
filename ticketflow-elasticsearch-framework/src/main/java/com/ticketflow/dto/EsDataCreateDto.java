package com.ticketflow.dto;

import lombok.Data;

/**
 * @Description: elasticsearch数据参数
 * @Author: rickey-c
 * @Date: 2025/2/2 20:33
 */
@Data
public class EsDataCreateDto {

    /**
     * 字段名
     */
    private String paramName;
    /**
     * 字段值
     */
    private Object paramValue;
}