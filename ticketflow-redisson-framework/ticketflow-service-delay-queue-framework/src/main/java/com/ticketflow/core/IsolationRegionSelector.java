package com.ticketflow.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/2/2 23:58
 */
public class IsolationRegionSelector {

    private final AtomicInteger count = new AtomicInteger(0);

    private final Integer thresholdValue;

    public IsolationRegionSelector(Integer thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    private int reset() {
        count.set(0);
        return count.get();
    }

    public synchronized int getIndex() {
        int cur = count.get();
        if (cur >= thresholdValue) {
            cur = reset();
        } else {
            cur = count.incrementAndGet();
        }
        return cur;
    }

}
