package com.ticketflow.dto;

import lombok.Data;

/**
 * @Description: 票据数据操作 dto
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
public class TicketCategoryCountDto {

    /**
     * 票档id
     */
    private Long ticketCategoryId;

    /**
     * 数量
     */
    private Long count;
}
