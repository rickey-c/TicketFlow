package com.ticketflow.execute;

import com.ticketflow.execute.base.AbstractApplicationExecute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_INITIALIZING_BEAN;

/**
 * @Description: 统一处理 {@link InitializingBean} 应用程序启动事件
 * @Author: rickey-c
 * @Date: 2025/1/30 22:16
 */
public class ApplicationInitializingBeanExecute extends AbstractApplicationExecute implements InitializingBean {

    public ApplicationInitializingBeanExecute(ConfigurableApplicationContext applicationContext){
        super(applicationContext);
    }

    @Override
    public String type() {
        return APPLICATION_INITIALIZING_BEAN;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        execute();
    }
}
