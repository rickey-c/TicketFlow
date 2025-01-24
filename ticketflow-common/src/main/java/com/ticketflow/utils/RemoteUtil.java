package com.ticketflow.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 * @Description: 远程IP工具类
 * @Author: rickey-c
 * @Date: 2025/1/23 16:43
 */
public class RemoteUtil {

    public static String getRemoteId(HttpServletRequest request) {
        String forward = request.getHeader("X-Forwarded-For");
        String ip = getRemoteIpFromForward(forward);
        String ua = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(ip)) {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

    private static String getRemoteIpFromForward(String forward) {
        if (StringUtils.isNotBlank(forward)) {
            String[] ipList = forward.split(",");
            return StringUtils.trim(ipList[0]);
        }
        return null;
    }
}
