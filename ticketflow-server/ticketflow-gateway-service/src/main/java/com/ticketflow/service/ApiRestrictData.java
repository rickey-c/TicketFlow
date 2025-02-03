package com.ticketflow.service;

import lombok.Data;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/29 20:51
 */
@Data
public class ApiRestrictData {

    private Long triggerResult;

    private Long triggerCallStat;

    private Long apiCount;

    /**
     * 门槛
     */
    private Long threshold;

    private Long messageIndex;
}
