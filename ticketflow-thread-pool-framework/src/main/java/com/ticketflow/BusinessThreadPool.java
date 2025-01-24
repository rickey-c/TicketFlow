package com.ticketflow;

import com.ticketflow.base.BaseThreadPool;
import com.ticketflow.namefactory.BusinessNameThreadFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.*;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/23 15:34
 */
public class BusinessThreadPool extends BaseThreadPool {

    private static ThreadPoolExecutor executor = null;

    static {
        executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() + 1,// 核心线程数
                maximumPoolSize(),// 最大线程数
                60,// 空闲线程存活时间
                TimeUnit.SECONDS,// 时间单位
                new ArrayBlockingQueue<>(600),// 工作队列
                new BusinessNameThreadFactory(),// 线程工厂
                new ThreadPoolExecutor.AbortPolicy());// 拒绝策略
    }

    /**
     * 最大线程数=CPU核心数/0.2
     *
     * @return
     */
    private static Integer maximumPoolSize() {
        return new BigDecimal(Runtime.getRuntime().availableProcessors())
                .divide(new BigDecimal("0.2"), 0, RoundingMode.HALF_UP).intValue();
    }

    public static void execute(Runnable r) {
        executor.execute(wrapTask(r, getContextForTask(), getContextForHold()));
    }

    public static <T> Future<T> submit(Callable<T> c) {
        return executor.submit(wrapTask(c, getContextForTask(), getContextForHold()));
    }
}
