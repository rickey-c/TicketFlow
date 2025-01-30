package com.ticketflow.impl.composite.init;

import com.ticketflow.base.AbstractApplicationStartEventListenerHandler;
import com.ticketflow.impl.composite.CompositeContainer;
import lombok.AllArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Description: 组合模式初始化操作执行
 * @Author: rickey-c
 * @Date: 2025/1/30 23:12
 */
@AllArgsConstructor
public class CompositeInit extends AbstractApplicationStartEventListenerHandler {
    
    private final CompositeContainer compositeContainer;
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
    
    @Override
    public void executeInit(ConfigurableApplicationContext context) {
        compositeContainer.init(context);
    }
}
