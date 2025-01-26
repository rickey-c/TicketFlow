package com.ticketflow.config;

import cn.hutool.core.lang.Snowflake;
import com.ticketflow.toolkit.SnowflakeIdGenerator;
import com.ticketflow.toolkit.WorkAndDataIdCenterHandler;
import com.ticketflow.toolkit.WorkDataCenterId;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Description: 自动装配类
 * @Author: rickey-c
 * @Date: 2025/1/26 12:52
 */
@Component
public class IdGeneratorConfig {

    @Bean
    public WorkAndDataIdCenterHandler workAndDataIdCenterHandler(StringRedisTemplate stringRedisTemplate) {
        return new WorkAndDataIdCenterHandler(stringRedisTemplate);
    }

    @Bean
    public WorkDataCenterId workDataCenterId(WorkAndDataIdCenterHandler workAndDataIdCenterHandler) {
        return workAndDataIdCenterHandler.getWorkAndDataCenterId();
    }

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(WorkDataCenterId workDataCenterId) {
        return new SnowflakeIdGenerator(workDataCenterId);
    }

}
