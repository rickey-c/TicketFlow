package com.ticketflow.toolkit;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 生成workId和dataCenterId的类
 * @Author: rickey-c
 * @Date: 2025/1/26 12:54
 */
@Slf4j
public class WorkAndDataIdCenterHandler {

    private final String SNOWFLAKE_WORK_ID_KEY = "snowflake_work_id";

    private final String SNOWFLAKE_DATA_CENTER_ID_KEY = "snowflake_data_center_id";

    public final List<String> keys = Arrays.asList(SNOWFLAKE_WORK_ID_KEY, SNOWFLAKE_DATA_CENTER_ID_KEY);

    private final StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<String> redisScript;
    
    public WorkAndDataIdCenterHandler(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        try {
            redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/workAndDataCenterId.lua")));
            redisScript.setResultType(String.class);
        } catch (Exception e) {
            log.error("redisScript init lua error", e);
        }
    }

    public WorkDataCenterId getWorkAndDataCenterId() {
        WorkDataCenterId workDataCenterId = new WorkDataCenterId();
        try {
            Object[] data = new Object[2];
            data[0] = String.valueOf(IdGeneratorConstant.MAX_WORKER_ID);
            data[1] = String.valueOf(IdGeneratorConstant.MAX_DATA_CENTER_ID);
            String result = stringRedisTemplate.execute(redisScript, keys, data);
            workDataCenterId = JSON.parseObject(result, WorkDataCenterId.class);
        } catch (Exception e) {
            log.error("getWorkAndDataCenterId error", e);
        }
        return workDataCenterId;
    }
}
