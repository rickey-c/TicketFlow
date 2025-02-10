package com.ticketflow.pay;

import com.alipay.api.*;
import com.ticketflow.pay.alipay.AlipayStrategyHandler;
import com.ticketflow.pay.alipay.config.AlipayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 自动配置类
 * @Author: rickey-c
 * @Date: 2025/2/10 11:48
 */
@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class PayAutoConfig {
    
    @Bean
    public AlipayClient alipayClient(AlipayProperties alipayProperties) throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(alipayProperties.getGatewayUrl());
        alipayConfig.setAppId(alipayProperties.getAppId());
        alipayConfig.setPrivateKey(alipayProperties.getMerchantPrivateKey());
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        alipayConfig.setAlipayPublicKey(alipayProperties.getAlipayPublicKey());
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);
        //构造client
        return new DefaultAlipayClient(alipayConfig);
    }

    @Bean
    public PayStrategyContext payStrategyContext(){
        return new PayStrategyContext();
    }

    @Bean
    public PayStrategyInitHandler payStrategyInitHandler(PayStrategyContext payStrategyContext){
        return new PayStrategyInitHandler(payStrategyContext);
    }

    @Bean
    public AlipayStrategyHandler alipayCall(AlipayClient alipayClient, AlipayProperties aliPayProperties){
        return new AlipayStrategyHandler(alipayClient,aliPayProperties);
    }
}
