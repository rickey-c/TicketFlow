package com.ticketflow.service.lua;

import com.alibaba.fastjson.JSON;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.vo.SeatVo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 查询座位缓存
 * @Author: rickey-c
 * @Date: 2025/2/7 15:07
 */
@Slf4j
@Component
public class ProgramSeatCacheData {

    @Autowired
    private RedisCache redisCache;

    private DefaultRedisScript redisScript;

    private static final Integer THRESHOLD_VALUE = 2000;

    @PostConstruct
    public void init() {
        try {
            redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/programSeat.lua")));
            redisScript.setResultType(Object.class);
        } catch (Exception e) {
            log.error("redisScript init lua error", e);
        }
    }

    public List<SeatVo> getData(List<String> keys, String[] args) {
        List<SeatVo> list;
        Object object = redisCache.getInstance().execute(redisScript, keys, args);
        List<String> seatVoStrlist = new ArrayList<>();
        if (Objects.nonNull(object) && object instanceof ArrayList) {
            seatVoStrlist = (ArrayList<String>) object;
        }
        if (seatVoStrlist.size() > THRESHOLD_VALUE) {
            list = seatVoStrlist.parallelStream()
                    .map(seatVoStr -> JSON.parseObject(seatVoStr, SeatVo.class)).collect(Collectors.toList());
        } else {
            list = seatVoStrlist.stream()
                    .map(seatVoStr -> JSON.parseObject(seatVoStr, SeatVo.class)).collect(Collectors.toList());
        }
        return list;
    }
}
