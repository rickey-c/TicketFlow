package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 票档 实体
 * @Author: rickey-c
 * @Date: 2025/2/3 19:51
 */
@Data
@TableName("d_ticket_category")
public class TicketCategory extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 节目表id
     */
    private Long programId;

    /**
     * 介绍
     */
    private String introduce;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 总数量
     */
    private Long totalNumber;

    /**
     * 剩余数量
     */
    private Long remainNumber;


}
