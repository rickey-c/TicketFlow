package com.ticketflow.service;

import com.alibaba.fastjson.JSONObject;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.jwt.TokenUtil;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @Description: token服务
 * @Author: rickey-c
 * @Date: 2025/1/29 20:51
 */
@Service
public class TokenService {

    @Autowired
    private RedisCache redisCache;

    public String parseToken(String token, String tokenSecret) {
        String userStr = TokenUtil.parseToken(token, tokenSecret);
        if (StringUtil.isNotEmpty(userStr)) {
            return JSONObject.parseObject(userStr).getString("userId");
        }
        return null;
    }

    public UserVo getUser(String token, String code, String tokenSecret) {
        UserVo userVo = null;
        String userId = parseToken(token, tokenSecret);
        if (StringUtil.isNotEmpty(userId)) {
            userVo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, userId), UserVo.class);
        }
        return Optional.ofNullable(userVo).orElseThrow(() -> new TicketFlowFrameException(BaseCode.LOGIN_USER_NOT_EXIST));
    }
}
