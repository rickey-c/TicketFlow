package com.ticketflow.core;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static com.ticketflow.constant.Constant.DEFAULT_PREFIX_DISTINCTION_NAME;
import static com.ticketflow.constant.Constant.PREFIX_DISTINCTION_NAME;

/**
 * @Description: spring工具
 * @Author: rickey-c
 * @Date: 2025/1/25 13:36
 */
public class SpringUtil implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static ConfigurableApplicationContext configurableApplicationContext;

    /**
     * 获取前缀名称
     * @return
     */
    public static String getPrefixDistinctionName(){
        return configurableApplicationContext.getEnvironment().getProperty(PREFIX_DISTINCTION_NAME,
                DEFAULT_PREFIX_DISTINCTION_NAME);
    }

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        configurableApplicationContext = applicationContext;
    }
}