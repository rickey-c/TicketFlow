package com.ticketflow.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @Description: 过滤器配置
 * @Author: rickey-c
 * @Date: 2025/1/23 15:32
 */
@Configuration
public class FilterConfig {

    @Bean
    public OncePerRequestFilter requestParamContextFilter() {
        return new RequestParamContextFilter();
    }
}
