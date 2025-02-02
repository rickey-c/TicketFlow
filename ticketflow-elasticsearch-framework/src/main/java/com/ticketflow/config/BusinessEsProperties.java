package com.ticketflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @Description: elasticsearchGeo配置属性
 * @Author: rickey-c
 * @Date: 2025/2/2 21:28
 */
@Data
@ConfigurationProperties(prefix = BusinessEsProperties.PREFIX)
public class BusinessEsProperties {

    public static final String PREFIX = "elasticsearch";

    private String[] ip;

    private String userName;

    private String passWord;

    private Boolean esSwitch = true;

    private Boolean esTypeSwitch = false;

    private Integer connectTimeOut = 40000;

    private Integer socketTimeOut = 40000;

    private Integer connectionRequestTimeOut = 40000;

    private Integer maxConnectNum = 400;
}
