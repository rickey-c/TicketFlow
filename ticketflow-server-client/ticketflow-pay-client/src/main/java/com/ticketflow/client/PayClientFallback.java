package com.ticketflow.client;


import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.NotifyDto;
import com.ticketflow.dto.PayDto;
import com.ticketflow.dto.RefundDto;
import com.ticketflow.dto.TradeCheckDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.vo.NotifyVo;
import com.ticketflow.vo.TradeCheckVo;
import org.springframework.stereotype.Component;

/**
 * @Description: 支付服务 feign 异常
 * @Author: rickey-c
 * @Date: 2025/2/9 16:55
 */
@Component
public class PayClientFallback implements PayClient {

    @Override
    public ApiResponse<String> commonPay(final PayDto payDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<NotifyVo> notify(final NotifyDto notifyDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<TradeCheckVo> tradeCheck(final TradeCheckDto tradeCheckDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<String> refund(final RefundDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
