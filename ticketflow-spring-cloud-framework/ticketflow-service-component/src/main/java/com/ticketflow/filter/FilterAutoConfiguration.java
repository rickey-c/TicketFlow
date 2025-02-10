package com.ticketflow.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @Description: 自动装配过过滤器
 * @Author: rickey-c
 * @Date: 2025/1/25 17:02
 */
@Component
public class FilterAutoConfiguration {

    @Bean
    @Order(-10)
    public RequestWrapperFilter requestWrapperFilter() {
        return new RequestWrapperFilter();
    }

    @Bean
    @Order(1)
    public BaseParameterFilter baseParameterFilter() {
        return new BaseParameterFilter();
    }


}
