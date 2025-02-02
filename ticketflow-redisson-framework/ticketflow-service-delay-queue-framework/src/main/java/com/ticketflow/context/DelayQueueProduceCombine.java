package com.ticketflow.context;

import com.ticketflow.core.DelayProduceQueue;
import com.ticketflow.core.IsolationRegionSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 延迟队列-生产者-分片选择
 * @Author: rickey-c
 * @Date: 2025/2/3 0:02
 */
public class DelayQueueProduceCombine {

    private final IsolationRegionSelector isolationRegionSelector;

    private final List<DelayProduceQueue> delayProduceQueueList = new ArrayList<>();

    public DelayQueueProduceCombine(DelayQueueBasePart delayQueueBasePart, String topic) {
        Integer isolationRegionCount = delayQueueBasePart.getDelayQueueProperties().getIsolationRegionCount();
        isolationRegionSelector = new IsolationRegionSelector(isolationRegionCount);
        for (int i = 0; i < isolationRegionCount; i++) {
            delayProduceQueueList.add(new DelayProduceQueue(delayQueueBasePart.getRedissonClient(), topic + "-" + i));
        }
    }

    public void offer(String content, long delayTime, TimeUnit timeUnit) {
        int index = isolationRegionSelector.getIndex();
        delayProduceQueueList.get(index).offer(content, delayTime, timeUnit);
    }

}
