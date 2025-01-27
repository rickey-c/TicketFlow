package com.ticketflow.config;

import com.ticketflow.toolkit.SnowflakeIdGenerator;
import com.ticketflow.toolkit.WorkAndDataIdCenterHandler;
import com.ticketflow.toolkit.WorkDataCenterId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * @Description: 自动装配类
 * @Author: rickey-c
 * @Date: 2025/1/26 12:52
 */
@Configuration
public class IdGeneratorAutoConfig {

    @Bean
    @Order(-15)
    public WorkAndDataIdCenterHandler workAndDataIdCenterHandler(StringRedisTemplate stringRedisTemplate) {
        return new WorkAndDataIdCenterHandler(stringRedisTemplate);
    }

    @Bean
    @Order(-14)
    public WorkDataCenterId workDataCenterId(WorkAndDataIdCenterHandler workAndDataIdCenterHandler) {
        return workAndDataIdCenterHandler.getWorkAndDataCenterId();
    }

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(WorkDataCenterId workDataCenterId) {
        return new SnowflakeIdGenerator(workDataCenterId);
    }

}
