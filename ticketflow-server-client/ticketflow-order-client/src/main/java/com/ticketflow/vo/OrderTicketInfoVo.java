package com.ticketflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: 购票订单信息 vo
 * @Author: rickey-c
 * @Date: 2025/12/3 14:41
 */
@Data
@Schema(title="OrderTicketInfoVo", description ="购票订单信息")
public class OrderTicketInfoVo {
    
    @Schema(name ="seatInfo", type ="String", description ="座位信息")
    private String seatInfo;
    
    @Schema(name ="price", type ="BigDecimal", description ="单价")
    private BigDecimal price;
    
    @Schema(name ="quantity", type ="Integer", description ="数量")
    private Integer quantity;
    
    @Schema(name ="favourablePrice", type ="BigDecimal", description ="优惠")
    private BigDecimal favourablePrice;
    
    @Schema(name ="relPrice", type ="BigDecimal", description ="小计")
    private BigDecimal relPrice;
    
}
