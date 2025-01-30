package com.ticketflow.vo;

import lombok.Data;

/**
 * @Description: 普通规则vo
 * @Author: rickey-c
 * @Date: 2025/1/29 21:35
 */
@Data
public class RuleVo {

    private String id;

    private Integer statTime;

    private Integer statTimeType;

    private Integer threshold;

    private Integer effectiveTime;

    private Integer effectiveTimeType;

    private String limitApi;

    private String message;

    private Integer status;
}
