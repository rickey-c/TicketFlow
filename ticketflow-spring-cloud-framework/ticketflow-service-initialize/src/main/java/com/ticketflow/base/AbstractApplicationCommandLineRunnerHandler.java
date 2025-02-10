package com.ticketflow.base;

import org.springframework.boot.CommandLineRunner;

import static com.ticketflow.constant.InitializeHandlerType.APPLICATION_COMMAND_LINE_RUNNER;

/**
 * @Description: 用于处理 {@link CommandLineRunner} 类型 初始化执行 抽象
 * @Author: rickey-c
 * @Date: 2025/1/30 21:44
 */
public abstract class AbstractApplicationCommandLineRunnerHandler implements InitializeHandler {

    @Override
    public String type() {
        return APPLICATION_COMMAND_LINE_RUNNER;
    }
}
