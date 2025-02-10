package com.ticketflow.impl.composite;

import com.ticketflow.impl.composite.init.CompositeInit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 组合模式自动配置
 * @Author: rickey-c
 * @Date: 2025/1/30 00:04
 */
@Configuration
public class CompositeAutoConfiguration {

    @Bean
    public CompositeContainer compositeContainer() {
        return new CompositeContainer();
    }

    @Bean
    public CompositeInit compositeInit(CompositeContainer compositeContainer) {
        return new CompositeInit(compositeContainer);
    }
}
