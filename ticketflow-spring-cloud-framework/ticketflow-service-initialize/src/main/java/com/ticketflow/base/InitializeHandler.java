package com.ticketflow.base;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/30 21:44
 */
public interface InitializeHandler {

    /**
     * 初始化类型
     * @return 类型
     */
    String type();

    /** 
     * 执行顺序
     * @return 顺序
     */
    Integer executeOrder();

    /**
     * 执行逻辑
     * @param context 容器上下文
     */
    void executeInit(ConfigurableApplicationContext context);
}
