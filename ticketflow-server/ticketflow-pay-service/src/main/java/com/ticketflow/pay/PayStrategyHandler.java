package com.ticketflow.pay;

import com.ticketflow.entity.PayBill;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Description: 支付方式抽象
 * @Author: rickey-c
 * @Date: 2025/2/10 11:36
 */
public interface PayStrategyHandler {
    /**
     * 支付
     * @param outTradeNo 订单号
     * @param price 支付价格
     * @param subject 标题
     * @param notifyUrl 回调地址
     * @param returnUrl 支付后返回地址
     * @return 结果
     * */
    PayResult pay(String outTradeNo, BigDecimal price, String subject, String notifyUrl, String returnUrl);
    
    /**
     * 验签
     * @param params 参数
     * @return 结果
     * */
    boolean signVerify(Map<String, String> params);
    
    /**
     * 数据验证
     * @param params 参数
     * @param payBill 支付账单
     * @return 结果
     * */
    boolean dataVerify(Map<String, String> params, PayBill payBill);
    
    /**
     * 状态查询
     * @param outTradeNo 订单号
     * @return 结果
     * */
    TradeResult queryTrade(String outTradeNo);
    
    /**
     * 退款
     * @param outTradeNo 订单号
     * @param price 支付价格
     * @param reason 原因
     * @return 结果
     * */
    RefundResult refund(String outTradeNo, BigDecimal price, String reason);
    
    /**
     * 支付渠道
     * @return 结果
     * */
    String getChannel();
}