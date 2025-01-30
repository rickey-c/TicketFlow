package com.ticketflow.execute.base;

import com.ticketflow.base.InitializeHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Comparator;
import java.util.Map;

/**
 * @Description: 应用初始化基类
 * @Author: rickey-c
 * @Date: 2025/1/30 22:06
 */
@AllArgsConstructor
public abstract class AbstractApplicationExecute {

    private final ConfigurableApplicationContext applicationContext;

    public void execute() {
        Map<String, InitializeHandler> initializeHandlerMap = applicationContext.getBeansOfType(InitializeHandler.class);
        initializeHandlerMap.values()
                .stream()
                .filter(initializeHandler -> initializeHandler.type().equals(type()))
                .sorted(Comparator.comparingInt(InitializeHandler::executeOrder))
                .forEach(initializeHandler -> {
                    initializeHandler.executeInit(applicationContext);
                });
    }
    
    public abstract String type();
}
