package com.ticketflow.execute;

import com.ticketflow.execute.base.AbstractApplicationExecute;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ConfigurableApplicationContext;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_POST_CONSTRUCT;

/**
 * @Description: 统一处理 {@link PostConstruct} 应用程序启动事件
 * @Author: rickey-c
 * @Date: 2025/1/30 22:20
 */
public class ApplicationPostConstructExecute extends AbstractApplicationExecute {

    public ApplicationPostConstructExecute(ConfigurableApplicationContext applicationContext) {
        super(applicationContext);
    }
    
    @PostConstruct
    public void postConstructExecute() {
        execute();
    }

    @Override
    public String type() {
        return APPLICATION_POST_CONSTRUCT;
    }
    
}
