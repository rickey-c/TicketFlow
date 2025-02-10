package com.ticketflow.pay.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.WebUtils;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ticketflow.entity.PayBill;
import com.ticketflow.enums.AlipayTradeStatus;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.PayBillStatus;
import com.ticketflow.enums.PayChannel;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.pay.PayResult;
import com.ticketflow.pay.PayStrategyHandler;
import com.ticketflow.pay.RefundResult;
import com.ticketflow.pay.TradeResult;
import com.ticketflow.pay.alipay.config.AlipayProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Description: 支付宝支付
 * @Author: rickey-c
 * @Date: 2025/2/10 11:36
 */
@Slf4j
@AllArgsConstructor
public class AlipayStrategyHandler implements PayStrategyHandler {

    /**
     * 支付宝的SDK
     * */
    private final AlipayClient alipayClient;
    
    /**
     * 支付宝相关配置
     * */
    private final AlipayProperties aliPayProperties;
    
    @Override
    public PayResult pay(String outTradeNo, BigDecimal price, String subject, String notifyUrl, String returnUrl){
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            //异步接收地址，仅支持http/https，公网可访问
            request.setNotifyUrl(notifyUrl);
            //同步跳转地址，仅支持http/https
            request.setReturnUrl(returnUrl);
            //必传参数
            JSONObject bizContent = new JSONObject();
            //商户订单号，商家自定义，保持唯一性
            bizContent.put("out_trade_no", outTradeNo);
            //支付金额，最小值0.01元
            bizContent.put("total_amount", price);
            //订单标题，不可使用特殊符号
            bizContent.put("subject", subject);
            //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(bizContent.toString());
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request,"POST");
            return new PayResult(response.isSuccess(),response.getBody());
        }catch (Exception e) {
           log.error("alipay pay error",e);
           throw new TicketFlowFrameException(BaseCode.PAY_ERROR);
        }
    }
    
    @Override
    public boolean signVerify(final Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    aliPayProperties.getAlipayPublicKey(),
                    AlipayConstants.CHARSET_UTF8,
                    //调用SDK验证签名
                    AlipayConstants.SIGN_TYPE_RSA2);
        }catch (Exception e) {
            log.error("alipay sign verify error",e);
            return false;
        }
        
    }
    
    @Override
    public boolean dataVerify(final Map<String, String> params, PayBill payBill) {
        //2 判断 total_amount 是否确实为该订单的实际金额（即商户订单创建时的金额）
        BigDecimal notifyPayAmount = new BigDecimal(params.get("total_amount"));
        BigDecimal payAmount = payBill.getPayAmount();
        if (notifyPayAmount.compareTo(payAmount) != 0) {
            log.error("回调金额和账单支付金额不一致 回调金额 : {}, 账单支付金额 : {}",notifyPayAmount,payAmount);
            return false;
        }
        //3 校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方
        String notifySellerId = params.get("seller_id");
        String alipaySellerId = aliPayProperties.getSellerId();
        if (!notifySellerId.equals(alipaySellerId)) {
            log.error("回调商户pid和已配置商户pid不一致 回调商户pid : {}, 已配置商户pid : {}",notifySellerId,alipaySellerId);
            return false;
        }
        //4 验证 app_id 是否为该商户本身
        String notifyAppId = params.get("app_id");
        String alipayAppId = aliPayProperties.getAppId();
        if(!notifyAppId.equals(alipayAppId)){
            log.error("回调appId和已配置appId不一致 回调appId : {}, 已配置appId : {}",notifyAppId,alipayAppId);
            return false;
        }
        //在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS时，支付宝才会认定为买家付款成功
        String tradeStatus = params.get("trade_status");
        if(!AlipayTradeStatus.TRADE_SUCCESS.getValue().equals(tradeStatus)){
            log.error("支付未成功 tradeStatus : {}",tradeStatus);
            return false;
        }
        return true;
    }
    
    @Override
    public TradeResult queryTrade(String outTradeNo) {
        String successCode = "10000";
        String successMsg = "Success";
        TradeResult tradeResult = new TradeResult();
        tradeResult.setSuccess(false);
        try {
            //构建查询参数，将订单号放入，调用SDK查询
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            WebUtils.setNeedCheckServerTrusted(false);
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", outTradeNo);
            request.setBizContent(bizContent.toString());
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                JSONObject jsonResponse = JSON.parseObject(response.getBody());
                JSONObject alipayTradeQueryResponse = jsonResponse.getJSONObject("alipay_trade_query_response");
                String code = alipayTradeQueryResponse.getString("code");
                String msg = alipayTradeQueryResponse.getString("msg");
                //如果调用成功
                if (successCode.equals(code) && successMsg.equals(msg)) {
                    tradeResult.setSuccess(true);
                    //订单编号
                    tradeResult.setOutTradeNo(alipayTradeQueryResponse.getString("out_trade_no"));
                    //支付金额
                    tradeResult.setTotalAmount(new BigDecimal(alipayTradeQueryResponse.getString("total_amount")));
                    //账单状态，需将支付的状态转换为对应的支付服务中账单状态
                    tradeResult.setPayBillStatus(convertPayBillStatus(alipayTradeQueryResponse.getString("trade_status")));
                    return tradeResult;
                }
            }else {
                log.error("支付宝交易查询结果失败 response : {}",JSON.toJSONString(response));
            }
        }catch (Exception e) {
            log.error("alipay trade query error",e);
        }
        return tradeResult;
    }
    
    @Override
    public RefundResult refund(String outTradeNo, BigDecimal price, String reason) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("refund_amount", price);
        bizContent.put("refund_reason", reason);
        
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            return new RefundResult(response.isSuccess(),response.getBody(),response.getMsg());
        } catch (AlipayApiException e) {
            log.error("alipay refund error",e);
            throw new TicketFlowFrameException(BaseCode.REFUND_ERROR);
        }
    }
    
    @Override
    public String getChannel() {
        return PayChannel.ALIPAY.getValue();
    }
    
    /**
     * 转换账单状态
     * */
    private Integer convertPayBillStatus(String tradeStatus){
        if (AlipayTradeStatus.WAIT_BUYER_PAY.getValue().equals(tradeStatus)) {
            return PayBillStatus.NO_PAY.getCode();
        } else if (AlipayTradeStatus.TRADE_CLOSED.getValue().equals(tradeStatus)) {
            return PayBillStatus.CANCEL.getCode();
        } else if (AlipayTradeStatus.TRADE_SUCCESS.getValue().equals(tradeStatus) || 
                AlipayTradeStatus.TRADE_FINISHED.getValue().equals(tradeStatus)) {
            return PayBillStatus.PAY.getCode();
        }
        throw new TicketFlowFrameException(BaseCode.ALIPAY_TRADE_STATUS_NOT_EXIST);
    }
}
