package com.ticketflow.event;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketflow.context.DelayQueueBasePart;
import com.ticketflow.context.DelayQueuePart;
import com.ticketflow.core.ConsumerTask;
import com.ticketflow.core.DelayConsumerQueue;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;

/**
 * @Description: 处理应用程序启动事件
 * @Author: rickey-c
 * @Date: 2025/2/3 0:13
 */
@AllArgsConstructor
public class DelayQueueInitHandler implements ApplicationListener<ApplicationStartedEvent> {

    private final DelayQueueBasePart delayQueueBasePart;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Map<String, ConsumerTask> consumerTaskMap = event.getApplicationContext().getBeansOfType(ConsumerTask.class);
        if (CollectionUtil.isEmpty(consumerTaskMap)) {
            return;
        }
        for (ConsumerTask consumerTask : consumerTaskMap.values()) {
            DelayQueuePart delayQueuePart = new DelayQueuePart(delayQueueBasePart, consumerTask);
            Integer isolationRegionCount = delayQueuePart.getDelayQueueBasePart().getDelayQueueProperties().getIsolationRegionCount();

            for (Integer i = 0; i < isolationRegionCount; i++) {
                DelayConsumerQueue delayConsumerQueue = new DelayConsumerQueue(delayQueuePart,
                        delayQueuePart.getConsumerTask().topic() + "-" + i);
                delayConsumerQueue.listenStart();
            }
        }
    }
}
