package com.ticketflow.service.lua;


import com.ticketflow.redis.RedisCache;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 节目订单 缓存
 * @Author: rickey-c
 * @Date: 2025/2/7 14:55
 */
@Slf4j
@Component
public class ProgramCacheResolutionOperate {

    @Autowired
    private RedisCache redisCache;

    private DefaultRedisScript redisScript;

    @PostConstruct
    public void init() {
        try {
            redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/programDataResolution.lua")));
            redisScript.setResultType(Integer.class);
        } catch (Exception e) {
            log.error("redisScript init lua error", e);
        }
    }

    public void programCacheOperate(List<String> keys, String[] args) {
        redisCache.getInstance().execute(redisScript, keys, args);
    }
}
