package com.rickey.ticketflow.namefactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description: 抽象线程工厂类，用于自定义线程工厂的实现
 * @Author: rickey-c
 * @Date: 2025/1/23 15:33
 */
public abstract class AbstractNameThreadFactory implements ThreadFactory {

    // 生成全局唯一的线程池编号
    protected static final AtomicLong POOL_NUM = new AtomicLong(1);
    // 线程组，用于管理线程的集合
    private final ThreadGroup group;
    // 线程编号
    private final AtomicLong threadNum = new AtomicLong(1);
    // 线程名称前缀
    private String namePrefix = "";

    public AbstractNameThreadFactory() {
        group = Thread.currentThread().getThreadGroup();
        namePrefix = getNamePrefix() + "--thread--";
    }

    /**
     * 子类实现获取线程池名称的前缀
     *
     * @return String
     */
    public abstract String getNamePrefix();

    /**
     * 创建新线程，并设置线程的名称和其他默认属性。
     * 例子:子类重写的namePrefix--thread--2(每个线程池中线程的数量)
     *
     * @param r 线程要执行的任务
     * @return 创建好的线程对象
     */
    @Override
    public Thread newThread(Runnable r) {
        String name = namePrefix + threadNum.getAndIncrement();
        Thread t = new Thread(group, r, name, 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}

