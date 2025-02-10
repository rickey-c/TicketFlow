package com.ticketflow.mq.callback;

/**
 * @Description: 执行失败情况
 * @Author: rickey-c
 * @Date: 2025/1/24 17:01
 */
@FunctionalInterface
public interface FailureCallback {

    /**
     * 执行逻辑
     *
     * @param ex 执行失败的异常当做参数传递
     */
    void onFailure(Throwable ex);

}