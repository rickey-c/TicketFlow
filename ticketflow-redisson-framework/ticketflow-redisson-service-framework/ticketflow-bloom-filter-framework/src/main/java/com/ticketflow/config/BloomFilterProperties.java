package com.ticketflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: 布隆过滤器配置属性
 * @Author: rickey-c
 * @Date: 2025/1/26 17:02
 */
@Data
@ConfigurationProperties(prefix = BloomFilterProperties.PREFIX)
public class BloomFilterProperties {
    public static final String PREFIX = "bloom-filter";
    
    private String name;

    private Long expectedInsertions = 20000L;

    private Double falseProbability = 0.01D;
}
