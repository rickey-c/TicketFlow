package com.ticketflow.client;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.NotifyDto;
import com.ticketflow.dto.PayDto;
import com.ticketflow.dto.RefundDto;
import com.ticketflow.dto.TradeCheckDto;
import com.ticketflow.vo.NotifyVo;
import com.ticketflow.vo.TradeCheckVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import static com.ticketflow.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;

/**
 * @Description: 支付服务 feign
 * @Author: rickey-c
 * @Date: 2025/2/9 16:52
 */
@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME + "-" + "pay-service", fallback = PayClientFallback.class)
public interface PayClient {
    /**
     * 支付
     *
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/pay/common/pay")
    ApiResponse<String> commonPay(PayDto dto);

    /**
     * 回调
     *
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/pay/notify")
    ApiResponse<NotifyVo> notify(NotifyDto dto);

    /**
     * 查询支付状态
     *
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/pay/trade/check")
    ApiResponse<TradeCheckVo> tradeCheck(TradeCheckDto dto);

    /**
     * 退款
     *
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/pay/refund")
    ApiResponse<String> refund(RefundDto dto);
}
