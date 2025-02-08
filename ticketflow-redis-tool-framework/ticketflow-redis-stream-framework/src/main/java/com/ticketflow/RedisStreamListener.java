package com.ticketflow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.stream.StreamListener;

/**
 * @Description: redis-stream监听
 * @Author: rickey-c
 * @Date: 2025/2/8 11:05
 */
@Slf4j
@AllArgsConstructor
public class RedisStreamListener implements StreamListener<String, ObjectRecord<String, String>> {

    private final MessageConsumer messageConsumer;
    
    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        try {
            RecordId messageId = message.getId();
            String value = message.getValue();
            log.info("redis stream 消费到了数据 messageId : {}, streamName : {}, message : {}",
                    messageId, message.getStream(), value);
            messageConsumer.accept(message);
        } catch (Exception e) {
            log.error("onMessage error", e);
        }
    }
}