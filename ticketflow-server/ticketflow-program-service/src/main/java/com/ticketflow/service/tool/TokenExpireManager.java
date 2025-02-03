package com.ticketflow.service.tool;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description: Token 失效时间管理
 * @Author: rickey-c
 * @Date: 2025/2/3 20:34
 */
@Data
@Component
public class TokenExpireManager {

    @Value("${token.expire.time:40}")
    private Long tokenExpireTime;

}