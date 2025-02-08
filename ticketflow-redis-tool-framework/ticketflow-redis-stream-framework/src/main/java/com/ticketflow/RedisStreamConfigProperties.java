package com.ticketflow;

import com.ticketflow.constant.RedisStreamConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: redis-stream属性配置
 * @Author: rickey-c
 * @Date: 2025/2/8 11:00
 */
@Data
@ConfigurationProperties(prefix = RedisStreamConfigProperties.PREFIX)
public class RedisStreamConfigProperties {

    public static final String PREFIX = "spring.data.redis.stream";

    /**
     * stream名字
     */
    private String streamName;

    /**
     * 消费组名字
     */
    private String consumerGroup;

    /**
     * 消费者名
     */
    private String consumerName;

    /**
     * 消费方式 group:消费组(默认)/broadcast:广播
     */
    private String consumerType = RedisStreamConstant.GROUP;
}
