package com.rickey.ticketflow.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/23 15:32
 */
public class FilterConfig {
    
    @Bean
    public OncePerRequestFilter requestParamContextFilter() {
        return new RequestParamContextFilter();
    }
}
