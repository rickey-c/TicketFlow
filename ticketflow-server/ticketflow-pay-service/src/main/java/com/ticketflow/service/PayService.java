package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.dto.*;
import com.ticketflow.entity.PayBill;
import com.ticketflow.entity.RefundBill;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.PayBillStatus;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.mapper.PayBillMapper;
import com.ticketflow.mapper.RefundBillMapper;
import com.ticketflow.pay.*;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.utils.DateUtils;
import com.ticketflow.vo.NotifyVo;
import com.ticketflow.vo.PayBillVo;
import com.ticketflow.vo.TradeCheckVo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.ticketflow.constant.Constant.ALIPAY_NOTIFY_FAILURE_RESULT;
import static com.ticketflow.constant.Constant.ALIPAY_NOTIFY_SUCCESS_RESULT;
import static com.ticketflow.core.DistributedLockConstants.COMMON_PAY;
import static com.ticketflow.core.DistributedLockConstants.TRADE_CHECK;

/**
 * @Description: 支付 service
 * @Author: rickey-c
 * @Date: 2025/2/10 09:33
 */
@Slf4j
@Service
public class PayService {

    @Autowired
    private PayBillMapper payBillMapper;

    @Autowired
    private RefundBillMapper refundBillMapper;

    @Autowired
    private PayStrategyContext payStrategyContext;

    @Autowired
    private UidGenerator uidGenerator;

    /**
     * 支付接口
     *
     * @param payDto 支付dto
     * @return 支付页面
     */
    @ServiceLock(name = COMMON_PAY, keys = {"#payDto.orderNumber"})
    @Transactional(rollbackFor = Exception.class)
    public String commonPay(@NotNull PayDto payDto) {
        LambdaQueryWrapper<PayBill> wrapper = Wrappers.lambdaQuery(PayBill.class)
                .eq(PayBill::getOutOrderNo, payDto.getOrderNumber());
        PayBill payBill = payBillMapper.selectOne(wrapper);
        if (Objects.nonNull(payBill) && !Objects.equals(payBill.getPayBillStatus(), PayBillStatus.NO_PAY.getCode())) {
            throw new TicketFlowFrameException(BaseCode.PAY_BILL_IS_NOT_NO_PAY);
        }
        PayStrategyHandler payStrategyHandler = payStrategyContext.get(payDto.getChannel());
        PayResult payResult = payStrategyHandler.pay(String.valueOf(payDto.getOrderNumber()), payDto.getPrice(),
                payDto.getSubject(), payDto.getReturnUrl(), payDto.getReturnUrl());
        if (payResult.isSuccess()) {
            payBill = new PayBill();
            payBill.setId(uidGenerator.getUid());
            payBill.setOutOrderNo(String.valueOf(payDto.getOrderNumber()));
            payBill.setPayChannel(payDto.getChannel());
            payBill.setPayScene("生产");
            payBill.setSubject(payDto.getSubject());
            payBill.setPayAmount(payDto.getPrice());
            payBill.setPayBillType(payDto.getPayBillType());
            payBill.setPayBillStatus(PayBillStatus.NO_PAY.getCode());
            payBill.setPayTime(DateUtils.now());
            payBillMapper.insert(payBill);
        }
        return payResult.getBody();
    }


    /**
     * 支付宝回调地址，更新订单状态
     *
     * @param notifyDto 回调dto
     * @return 回调vo
     */
    @Transactional(rollbackFor = Exception.class)
    public NotifyVo notify(NotifyDto notifyDto) {
        NotifyVo notifyVo = new NotifyVo();

        log.info("回调通知参数:{}", JSON.toJSONString(notifyDto));

        Map<String, String> params = notifyDto.getParams();
        PayStrategyHandler payStrategyHandler = payStrategyContext.get(notifyDto.getChannel());
        boolean signVerify = payStrategyHandler.signVerify(params);
        if (!signVerify) {
            notifyVo.setPayResult(ALIPAY_NOTIFY_FAILURE_RESULT);
            return notifyVo;
        }
        LambdaQueryWrapper<PayBill> wrapper = Wrappers.lambdaQuery(PayBill.class).eq(PayBill::getOutOrderNo, params.get("out_trade_no"));
        PayBill payBill = payBillMapper.selectOne(wrapper);
        // 判断账单是否存在
        if (Objects.isNull(payBill)) {
            log.error("账单为空 notifyDto = {}", JSON.toJSONString(notifyDto));
            notifyVo.setPayResult(ALIPAY_NOTIFY_FAILURE_RESULT);
            return notifyVo;
        }
        //如果账单已支付了，直接返回支付宝成功状态
        if (Objects.equals(payBill.getPayBillStatus(), PayBillStatus.PAY.getCode())) {
            log.info("账单已支付 notifyDto : {}", JSON.toJSONString(notifyDto));
            notifyVo.setOutTradeNo(payBill.getOutOrderNo());
            notifyVo.setPayResult(ALIPAY_NOTIFY_SUCCESS_RESULT);
            return notifyVo;
        }
        //如果账单已取消了，直接返回支付宝成功状态
        if (Objects.equals(payBill.getPayBillStatus(), PayBillStatus.CANCEL.getCode())) {
            log.info("账单已取消 notifyDto : {}", JSON.toJSONString(notifyDto));
            notifyVo.setOutTradeNo(payBill.getOutOrderNo());
            notifyVo.setPayResult(ALIPAY_NOTIFY_SUCCESS_RESULT);
            return notifyVo;
        }
        //如果账单已退单了，直接返回支付宝成功状态
        if (Objects.equals(payBill.getPayBillStatus(), PayBillStatus.REFUND.getCode())) {
            log.info("账单已退单 notifyDto : {}", JSON.toJSONString(notifyDto));
            notifyVo.setOutTradeNo(payBill.getOutOrderNo());
            notifyVo.setPayResult(ALIPAY_NOTIFY_SUCCESS_RESULT);
            return notifyVo;
        }
        //验证支付宝回调传入的参数，防止恶意攻击
        boolean dataVerify = payStrategyHandler.dataVerify(notifyDto.getParams(), payBill);
        if (!dataVerify) {
            notifyVo.setPayResult(ALIPAY_NOTIFY_FAILURE_RESULT);
            return notifyVo;
        }
        //更新账单为支付状态
        PayBill updatePayBill = new PayBill();
        updatePayBill.setPayBillStatus(PayBillStatus.PAY.getCode());
        LambdaUpdateWrapper<PayBill> payBillLambdaUpdateWrapper =
                Wrappers.lambdaUpdate(PayBill.class).eq(PayBill::getOutOrderNo, params.get("out_trade_no"));
        payBillMapper.update(updatePayBill, payBillLambdaUpdateWrapper);
        notifyVo.setOutTradeNo(payBill.getOutOrderNo());
        notifyVo.setPayResult(ALIPAY_NOTIFY_SUCCESS_RESULT);
        return notifyVo;
    }

    /**
     * 主动查询账单状态
     * @param tradeCheckDto 查询账单状态dto
     * @return 真实的账单状态
     */
    @Transactional(rollbackFor = Exception.class)
    @ServiceLock(name = TRADE_CHECK,keys = {"#tradeCheckDto.outTradeNo"})
    public TradeCheckVo tradeCheck(TradeCheckDto tradeCheckDto){
        TradeCheckVo tradeCheckVo = new TradeCheckVo();
        PayStrategyHandler payStrategyHandler = payStrategyContext.get(tradeCheckDto.getChannel());
        TradeResult tradeResult = payStrategyHandler.queryTrade(tradeCheckDto.getOutTradeNo());
        BeanUtil.copyProperties(tradeResult,tradeCheckVo);
        if (!tradeResult.isSuccess()){
            return tradeCheckVo;
        }
        BigDecimal totalAmount = tradeResult.getTotalAmount();
        String outTradeNo = tradeResult.getOutTradeNo();
        Integer payBillStatus = tradeResult.getPayBillStatus();
        PayBill payBill = payBillMapper.selectOne(Wrappers.lambdaQuery(PayBill.class)
                .eq(PayBill::getOutOrderNo, outTradeNo));
        if (payBill.getPayAmount().compareTo(totalAmount) != 0) {
            log.error("支付渠道 和库中账单支付金额不一致 支付渠道支付金额 : {}, 库中账单支付金额 : {}, tradeCheckDto : {}",
                    totalAmount,payBill.getPayAmount(),JSON.toJSONString(tradeCheckDto));
            return tradeCheckVo;
        }
        if (!Objects.equals(payBill.getPayBillStatus(), payBillStatus)) {
            log.warn("支付渠道和库中账单交易状态不一致 支付渠道payBillStatus : {}, 库中payBillStatus : {}, tradeCheckDto : {}",
                    payBillStatus,payBill.getPayBillStatus(),JSON.toJSONString(tradeCheckDto));
            PayBill updatePayBill = new PayBill();
            updatePayBill.setId(payBill.getId());
            updatePayBill.setPayBillStatus(payBillStatus);
            LambdaUpdateWrapper<PayBill> payBillLambdaUpdateWrapper =
                    Wrappers.lambdaUpdate(PayBill.class).eq(PayBill::getOutOrderNo, outTradeNo);
            // 更新，以支付渠道为准
            payBillMapper.update(updatePayBill,payBillLambdaUpdateWrapper);
            return tradeCheckVo;
        }
        return tradeCheckVo;
    }

    /**
     * 退款接口
     *
     * @param refundDto 退款dto
     * @return 退款消息
     */
    public String refund(@NotNull RefundDto refundDto) {
        PayBill payBill = payBillMapper.selectOne(Wrappers.lambdaQuery(PayBill.class)
                .eq(PayBill::getPayNumber, refundDto.getOrderNumber()));
        // 判空
        if (Objects.isNull(payBill)) {
            throw new TicketFlowFrameException(BaseCode.PAY_BILL_NOT_EXIST);
        }
        // 订单状态不是已支付
        if (!Objects.equals(payBill.getPayBillStatus(), PayBillStatus.PAY.getCode())) {
            throw new TicketFlowFrameException(BaseCode.PAY_BILL_IS_NOT_PAY_STATUS);
        }
        // 退款金额大于支付金额
        if (refundDto.getAmount().compareTo(payBill.getPayAmount()) > 0) {
            throw new TicketFlowFrameException(BaseCode.REFUND_AMOUNT_GREATER_THAN_PAY_AMOUNT);
        }
        PayStrategyHandler payStrategyHandler = payStrategyContext.get(refundDto.getChannel());
        RefundResult refundResult = payStrategyHandler.refund(
                refundDto.getOrderNumber(), refundDto.getAmount(), refundDto.getReason());
        if (refundResult.isSuccess()) {
            RefundBill refundBill = new RefundBill();
            refundBill.setId(uidGenerator.getUid());
            refundBill.setOutOrderNo(payBill.getOutOrderNo());
            refundBill.setPayBillId(payBill.getId());
            refundBill.setRefundAmount(refundDto.getAmount());
            refundBill.setRefundStatus(1);
            refundBill.setRefundTime(DateUtils.now());
            refundBill.setReason(refundDto.getReason());
            refundBillMapper.insert(refundBill);
            return refundBill.getOutOrderNo();
        } else {
            throw new TicketFlowFrameException(refundResult.getMessage());
        }
    }

    /**
     * 查看账单详细
     * @param payBillDto 账单dto
     * @return 账单vo
     */
    public PayBillVo detail(@NotNull PayBillDto payBillDto) {
        PayBillVo payBillVo = new PayBillVo();
        LambdaQueryWrapper<PayBill> payBillLambdaQueryWrapper =
                Wrappers.lambdaQuery(PayBill.class).eq(PayBill::getOutOrderNo, payBillDto.getOrderNumber());
        PayBill payBill = payBillMapper.selectOne(payBillLambdaQueryWrapper);
        if (Objects.nonNull(payBill)) {
            BeanUtil.copyProperties(payBill, payBillVo);
        }
        return payBillVo;
    }


}
