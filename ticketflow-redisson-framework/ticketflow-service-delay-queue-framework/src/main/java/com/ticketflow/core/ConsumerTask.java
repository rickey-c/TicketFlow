package com.ticketflow.core;

/**
 * @Description: 延迟队列 消费者接口
 * @Author: rickey-c
 * @Date: 2025/2/2 23:34
 */
public interface ConsumerTask {

    /**
     * 执行消费
     *
     * @param content 参数
     */
    void execute(String content);

    /**
     * 主题
     *
     * @return 主题
     */
    String topic();
}
