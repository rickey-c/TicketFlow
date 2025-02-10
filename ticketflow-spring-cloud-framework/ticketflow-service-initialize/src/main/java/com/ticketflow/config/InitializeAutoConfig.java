package com.ticketflow.config;

import com.ticketflow.execute.ApplicationCommandLineRunnerExecute;
import com.ticketflow.execute.ApplicationInitializingBeanExecute;
import com.ticketflow.execute.ApplicationPostConstructExecute;
import com.ticketflow.execute.ApplicationStartEventListenerExecute;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/30 22:24
 */
@Configuration
public class InitializeAutoConfig {

    @Bean
    public ApplicationInitializingBeanExecute applicationInitializingBeanExecute(
            ConfigurableApplicationContext applicationContext) {
        return new ApplicationInitializingBeanExecute(applicationContext);
    }

    @Bean
    public ApplicationPostConstructExecute applicationPostConstructExecute(
            ConfigurableApplicationContext applicationContext) {
        return new ApplicationPostConstructExecute(applicationContext);
    }

    @Bean
    public ApplicationStartEventListenerExecute applicationStartEventListenerExecute(
            ConfigurableApplicationContext applicationContext) {
        return new ApplicationStartEventListenerExecute(applicationContext);
    }

    @Bean
    public ApplicationCommandLineRunnerExecute applicationCommandLineRunnerExecute(
            ConfigurableApplicationContext applicationContext) {
        return new ApplicationCommandLineRunnerExecute(applicationContext);
    }
}
