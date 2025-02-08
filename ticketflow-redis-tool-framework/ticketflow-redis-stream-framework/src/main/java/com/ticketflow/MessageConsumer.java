package com.ticketflow;

import org.springframework.data.redis.connection.stream.ObjectRecord;

/**
 * @Description: redis-stream消息处理
 * @Author: rickey-c
 * @Date: 2025/2/8 10:59
 */
public interface MessageConsumer {
    
    void accept(ObjectRecord<String, String> message);
}
