package com.ticketflow.base;

import org.springframework.beans.factory.InitializingBean;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_INITIALIZING_BEAN;

/**
 * @Description: 用于处理 {@link InitializingBean} 类型 初始化执行 抽象
 * @Author: rickey-c
 * @Date: 2025/1/30 21:47
 */
public abstract class AbstractApplicationInitializingBeanHandler implements InitializeHandler {

    @Override
    public String type() {
        return APPLICATION_INITIALIZING_BEAN;
    }
    
}
