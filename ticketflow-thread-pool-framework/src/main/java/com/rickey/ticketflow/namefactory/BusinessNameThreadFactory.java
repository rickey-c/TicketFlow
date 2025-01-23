package com.rickey.ticketflow.namefactory;

/**
 * @Description: 业务线程工厂，制定了线程的前缀名
 * @Author: rickey-c
 * @Date: 2025/1/23 15:33
 */
public class BusinessNameThreadFactory extends AbstractNameThreadFactory {
    @Override
    public String getNamePrefix() {
        return "task-pool" + "--" + POOL_NUM.getAndIncrement();
    }
}
