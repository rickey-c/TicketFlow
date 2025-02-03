package com.ticketflow.limit;

import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 限流工具
 * @Author: rickey-c
 * @Date: 2025/1/30 13:49
 */
public class RateLimiter {

    private final Semaphore semaphore;
    private final TimeUnit timeUnit;

    public RateLimiter(int maxPermitsPerSecond) {
        this.timeUnit = TimeUnit.SECONDS;
        this.semaphore = new Semaphore(maxPermitsPerSecond);
    }

    public void acquire() throws InterruptedException {
        if (semaphore.tryAcquire(1, timeUnit)) {
            throw new TicketFlowFrameException(BaseCode.OPERATION_IS_TOO_FREQUENT_PLEASE_TRY_AGAIN_LATER);
        }
    }

    public void release() {
        semaphore.release();
    }
}
