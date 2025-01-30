package com.ticketflow.execute;

import com.ticketflow.execute.base.AbstractApplicationExecute;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_COMMAND_LINE_RUNNER;

/**
 * @Description: 统一处理 {@link CommandLineRunner} 应用程序启动事件
 * @Author: rickey-c
 * @Date: 2025/1/30 22:06
 */
public class ApplicationCommandLineRunnerExecute extends AbstractApplicationExecute implements CommandLineRunner {

    public ApplicationCommandLineRunnerExecute(ConfigurableApplicationContext applicationContext){
        super(applicationContext);
    }
    
    @Override
    public String type() {
        return APPLICATION_COMMAND_LINE_RUNNER;
    }

    @Override
    public void run(String... args) throws Exception {
        execute();
    }
}
