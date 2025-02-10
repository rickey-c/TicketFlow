package com.ticketflow.base;

import jakarta.annotation.PostConstruct;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_POST_CONSTRUCT;

/**
 * @Description: 用于处理 {@link PostConstruct} 类型 初始化执行 抽象
 * @Author: rickey-c
 * @Date: 2025/1/30 21:59
 */
public abstract class AbstractApplicationPostConstructHandler implements InitializeHandler {

    @Override
    public String type() {
        return APPLICATION_POST_CONSTRUCT;
    }

}
