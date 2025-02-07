package com.ticketflow.service.kafka;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description: kafka主题配置
 * @Author: rickey-c
 * @Date: 2025/2/7 21:25
 */
@Data
@Component
public class KafkaTopic {

    @Value("${spring.kafka.topic:default}")
    private String topic;
}
