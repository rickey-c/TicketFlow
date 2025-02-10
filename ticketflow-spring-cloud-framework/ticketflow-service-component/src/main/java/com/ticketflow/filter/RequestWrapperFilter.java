package com.ticketflow.filter;

import com.ticketflow.request.CustomizeRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @Description: request包装过滤器
 * @Author: rickey-c
 * @Date: 2025/1/25 17:02
 */
public class RequestWrapperFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CustomizeRequestWrapper customizeRequestWrapper = new CustomizeRequestWrapper(request);
        filterChain.doFilter(customizeRequestWrapper, response);
    }
}
