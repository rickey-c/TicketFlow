package com.ticketflow.base;

import org.springframework.boot.context.event.ApplicationStartedEvent;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_EVENT_LISTENER;

/**
 * @Description: 用于处理 {@link ApplicationStartedEvent} 类型 初始化执行 抽象
 * @Author: rickey-c
 * @Date: 2025/1/30 22:03
 */
public abstract class AbstractApplicationStartEventListenerHandler implements InitializeHandler {

    @Override
    public String type() {
        return APPLICATION_EVENT_LISTENER;
    }
}
