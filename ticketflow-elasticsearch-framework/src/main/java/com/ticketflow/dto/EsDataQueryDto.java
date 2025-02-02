package com.ticketflow.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Description: elasticsearch查询参数
 * @Author: rickey-c
 * @Date: 2025/2/2 20:38
 */
@Data
public class EsDataQueryDto {
    /**
     * 字段名
     */
    private String paramName;
    /**
     * 字段值
     */
    private Object paramValue;
    /**
     * 如果是时间类型，则不使用paramValue 使用startTime和endTime
     */
    private Date startTime;
    /**
     * 如果是时间类型，则不使用paramValue 使用startTime和endTime
     */
    private Date endTime;
    /**
     * 是否分词查询(默认不分词)
     */
    private boolean analyse = false;
}