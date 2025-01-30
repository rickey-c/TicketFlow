package com.ticketflow.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Description: 深度规则vo
 * @Author: rickey-c
 * @Date: 2025/1/29 21:35
 */
@Data
public class DepthRuleVo {

    private String id;

    private String startTimeWindow;

    private long startTimeWindowTimestamp;

    private String endTimeWindow;

    private long endTimeWindowTimestamp;

    private Integer statTime;

    private Integer statTimeType;

    private Integer threshold;

    private Integer effectiveTime;

    private Integer effectiveTimeType;

    private String limitApi;

    private String message;

    private Integer status;

    private Date createTime;
}