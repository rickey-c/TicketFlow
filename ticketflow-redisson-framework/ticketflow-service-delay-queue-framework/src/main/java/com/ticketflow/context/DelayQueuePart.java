package com.ticketflow.context;

import com.ticketflow.core.ConsumerTask;
import lombok.Data;

/**
 * @Description: 消费主题
 * @Author: rickey-c
 * @Date: 2025/2/2 23:46
 */
@Data
public class DelayQueuePart {

    private final DelayQueueBasePart delayQueueBasePart;

    private final ConsumerTask consumerTask;

    public DelayQueuePart(DelayQueueBasePart delayQueueBasePart, ConsumerTask consumerTask) {
        this.delayQueueBasePart = delayQueueBasePart;
        this.consumerTask = consumerTask;
    }
    
}
