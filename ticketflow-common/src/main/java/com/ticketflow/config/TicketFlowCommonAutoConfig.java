package com.ticketflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 通用模块自动装配类
 * @Author: rickey-c
 * @Date: 2025/1/23 16:47
 */
@Configuration
public class TicketFlowCommonAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustom() {
        return new JacksonCustom();
    }
}
