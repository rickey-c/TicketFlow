package com.ticketflow.feign;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class HeaderCacheService {

    @Cacheable(value = "headerCache", key = "#request.hashCode() + #headerName")
    public String getHeaderValue(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }
}