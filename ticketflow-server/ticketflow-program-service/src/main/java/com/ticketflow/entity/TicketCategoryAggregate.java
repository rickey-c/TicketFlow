package com.ticketflow.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 票档统计 实体
 * @Author: rickey-c
 * @Date: 2025/2/3 19:53
 */
@Data
public class TicketCategoryAggregate implements Serializable {

    /**
     * 节目表id
     */
    private Long programId;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;
}
