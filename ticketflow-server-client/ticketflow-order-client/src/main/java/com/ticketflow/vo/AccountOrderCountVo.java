package com.ticketflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 订单退款 vo
 * @Author: rickey-c
 * @Date: 2025/12/3 14:31
 */
@Data
@Schema(title = "AccountOrderCountVo", description = "账户下订单数量")
public class AccountOrderCountVo {

    @Schema(name = "count", type = "Integer", description = "账户下的订单数量")
    private Integer count;
}
