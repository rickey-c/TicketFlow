package com.ticketflow.conf;

import com.ticketflow.common.ApiResponse;
import lombok.Data;

import java.util.Map;

/**
 * @Description: 临时信息
 * @Author: rickey-c
 * @Date: 2025/1/30 14:16
 */
@Data
public class RequestTemporaryWrapper {
    
    private Map<String,String> map;
    
    private ApiResponse<?> apiResponse;
}
