package com.ticketflow.service.lua;

import com.alibaba.fastjson2.JSON;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.service.ApiRestrictData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 限流规则lua脚本执行类
 * @Author: rickey-c
 * @Date: 2025/1/29 20:52
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiRestrictCacheOperate {

    private final RedisCache redisCache;

    private DefaultRedisScript<String> redisScript;

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/apiLimit.lua")));
    }

    public ApiRestrictData apiRuleOperate(List<String> keys, Object[] args) {
        Object object = redisCache.getInstance().execute(redisScript, keys, args);
        return JSON.parseObject((String) object, ApiRestrictData.class);
    }
}
