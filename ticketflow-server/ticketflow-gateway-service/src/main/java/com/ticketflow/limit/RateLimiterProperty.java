package com.ticketflow.limit;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Description: 限流工具属性
 * @Author: rickey-c
 * @Date: 2025/1/30 13:49
 */
@Data
public class RateLimiterProperty {

    @Value("${rate.switch:false}")
    private Boolean rateSwitch;

    @Value("${rate.permits:200}")
    private Integer ratePermits;
}
