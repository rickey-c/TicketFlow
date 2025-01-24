package com.ticketflow.rejectedexcutionhandler;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description: 自定义拒绝策略
 * @Author: rickey-c
 * @Date: 2025/1/23 15:34
 */
public class ThreadPoolRejectedExecutionHandler {

    public static class BusinessAbortPolicy implements RejectedExecutionHandler {
        public BusinessAbortPolicy() {
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new RejectedExecutionException("threadPoolApplicationName business task " +
                    r.toString() +
                    " rejected from " +
                    executor.toString());
        }
    }
}
