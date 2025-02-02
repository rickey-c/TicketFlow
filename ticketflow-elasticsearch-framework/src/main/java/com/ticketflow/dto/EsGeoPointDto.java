package com.ticketflow.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: elasticsearchGeo数据
 * @Author: rickey-c
 * @Date: 2025/2/2 20:58
 */
@Data
public class EsGeoPointDto {
    /**
     * 字段名
     */
    private String paramName;
    /**
     * 纬度值
     */
    private BigDecimal latitude;
    /**
     * 经度值
     */
    private BigDecimal longitude;


}