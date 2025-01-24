package com.ticketflow.mq.callback;

/**
 * @Description: 执行成功情况
 * @Author: rickey-c
 * @Date: 2025/1/24 17:02
 */
@FunctionalInterface
public interface SuccessCallback<T> {
    
    /**
     * 执行逻辑
     * @param result 执行成功的结果当做参数传递
     * */
    void onSuccess(T result);

}
