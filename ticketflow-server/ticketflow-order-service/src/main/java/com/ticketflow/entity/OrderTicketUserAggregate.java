package com.ticketflow.entity;

import lombok.Data;

/**
 * @Description: 购票人聚合统计实体
 * @Author: rickey-c
 * @Date: 2025/2/9 17:10
 */
@Data
public class OrderTicketUserAggregate {

    private Long orderNumber;

    private Integer orderTicketUserCount;
}
