package com.ticketflow;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Objects;

/**
 * @Description: redis-stream操作
 * @Author: rickey-c
 * @Date: 2025/2/8 11:05
 */
@Slf4j
@AllArgsConstructor
public class RedisStreamHandler {

    private final RedisStreamPushHandler redisStreamPushHandler;

    private final StringRedisTemplate stringRedisTemplate;

    public void addGroup(String streamName, String groupName) {
        stringRedisTemplate.opsForStream().createGroup(streamName, groupName);
    }

    public Boolean hasKey(String hashKey) {
        if (Objects.isNull(hashKey)) {
            return false;
        } else {
            return stringRedisTemplate.hasKey(hashKey);
        }
    }


    public void del(String key, RecordId recordId) {
        stringRedisTemplate.opsForStream().delete(key, recordId);
    }

    public void streamBindingGroup(String streamName, String group) {
        boolean hasKey = hasKey(streamName);
        if (!hasKey) {
            HashMap<String, Object> map = new HashMap<>(2);
            map.put("key", "value");
            RecordId recordId = redisStreamPushHandler.push(JSON.toJSONString(map));
            addGroup(streamName, group);
            del(streamName, recordId);
            log.info("initStream streamName : {} group : {}", streamName, group);
        }
    }
}
