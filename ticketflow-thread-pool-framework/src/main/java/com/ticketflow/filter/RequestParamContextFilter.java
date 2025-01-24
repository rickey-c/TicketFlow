package com.ticketflow.filter;

import com.ticketflow.utils.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.ticketflow.constant.Constant.TRACE_ID;

/**
 * @Description: 链路过滤器，手动获取链路Id
 * @Author: rickey-c
 * @Date: 2025/1/23 15:32
 */
public class RequestParamContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID);
        if (StringUtil.isNotEmpty(traceId)) {
            MDC.put(TRACE_ID, traceId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }

    }
}
