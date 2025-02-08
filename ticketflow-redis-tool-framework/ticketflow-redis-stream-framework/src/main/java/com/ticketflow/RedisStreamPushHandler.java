package com.ticketflow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Description: redis-stream发送消息
 * @Author: rickey-c
 * @Date: 2025/2/8 11:02
 */
@Slf4j
@AllArgsConstructor
public class RedisStreamPushHandler {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisStreamConfigProperties redisStreamConfigProperties;

    public RecordId push(String msg) {
        ObjectRecord<String, String> record = StreamRecords.newRecord()
                .in(redisStreamConfigProperties.getStreamName())
                .ofObject(msg)
                .withId(RecordId.autoGenerate());
        RecordId recordId = stringRedisTemplate.opsForStream().add(record);
        log.info("redis streamName : {} message : {}", redisStreamConfigProperties.getStreamName(), msg);
        return recordId;
    }
}
