package com.ticketflow.execute;

import com.ticketflow.execute.base.AbstractApplicationExecute;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_EVENT_LISTENER;

/**
 * @Description: 统一处理 {@link ApplicationListener} 应用程序启动事件
 * @Author: rickey-c
 * @Date: 2025/1/30 22:23
 */
public class ApplicationStartEventListenerExecute extends AbstractApplicationExecute implements ApplicationListener<ApplicationStartedEvent> {

    public ApplicationStartEventListenerExecute(ConfigurableApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        execute();
    }

    @Override
    public String type() {
        return APPLICATION_EVENT_LISTENER;
    }
}
