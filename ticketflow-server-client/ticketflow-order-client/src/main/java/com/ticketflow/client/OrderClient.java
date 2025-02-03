package com.ticketflow.client;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.AccountOrderCountDto;
import com.ticketflow.dto.OrderCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

import static com.ticketflow.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;

/**
 * @Description: 订单服务 feign
 * @Author: rickey-c
 * @Date: 2025/2/3 18:08
 */
@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"order-service",fallback = OrderClientFallback.class)
public interface OrderClient {

    /**
     * 创建订单
     * @param orderCreateDto 创建订单 dto
     * @return 结果
     */
    ApiResponse<String> create(OrderCreateDto orderCreateDto);

    /**
     * 账户下某个节目的订单数量
     * @param accountOrderCountDto 订单数量 dto
     * @return 结果
     */
    ApiResponse<AccountOrderCountDto> accountOrderCount(AccountOrderCountDto accountOrderCountDto);
    
}
