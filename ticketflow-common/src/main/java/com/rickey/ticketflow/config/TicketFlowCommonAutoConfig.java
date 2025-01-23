package com.rickey.ticketflow.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * @Description: 通用模块自动装配类
 * @Author: rickey-c
 * @Date: 2025/1/23 16:47
 */
public class TicketFlowCommonAutoConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustom() {
        return new JacksonCustom();
    }
}
