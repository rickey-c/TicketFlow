package com.ticketflow.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static com.ticketflow.constant.Constant.SERVER_GRAY;

/**
 * @Description: feign扩展插件类-自动装配
 * @Author: rickey-c
 * @Date: 2025/1/25 16:58
 */
@Component
public class ExtraFeignAutoConfiguration {
    
    @Autowired
    private HeaderCacheService headerCacheService;

    @Value("${" + SERVER_GRAY + ":default-gray-value}")
    public String serverGray;

    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor(serverGray,headerCacheService);
    }
}
