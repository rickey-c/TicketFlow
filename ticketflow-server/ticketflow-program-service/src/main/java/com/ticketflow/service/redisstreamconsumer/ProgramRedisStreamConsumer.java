package com.ticketflow.service.redisstreamconsumer;

import com.ticketflow.MessageConsumer;
import com.ticketflow.service.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.stereotype.Component;

/**
 * @Description: redis-stream队列消费
 * @Author: rickey-c
 * @Date: 2025/2/9 15:03
 */
@Component
public class ProgramRedisStreamConsumer implements MessageConsumer {

    @Autowired
    private ProgramService programService;

    @Override
    public void accept(ObjectRecord<String, String> message) {
        Long programId = Long.parseLong(message.getValue());
        programService.delLocalCache(programId);
    }
}
