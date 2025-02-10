package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 支付账单 实体
 * @Author: rickey-c
 * @Date: 2025/2/10 08:14
 */
@Data
@TableName("d_pay_bill")
public class PayBill extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 支付流水号
     */
    private String payNumber;

    /**
     * 商户订单号
     */
    private String outOrderNo;

    /**
     * 支付渠道
     */
    private String payChannel;

    /**
     * 支付环境
     */
    private String payScene;

    /**
     * 订单标题
     */
    private String subject;

    /**
     * 三方交易凭证号
     */
    private String tradeNumber;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 支付种类
     * */
    private Integer payBillType;

    /**
     * 支付状态
     */
    private Integer payBillStatus;

    /**
     * 支付时间
     */
    private Date payTime;
}
