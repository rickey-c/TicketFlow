package com.ticketflow.kafka;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Description: kafka主题
 * @Author: rickey-c
 * @Date: 2025/1/29 20:55
 */
@Data
public class KafkaTopic {

    @Value("${spring.kafka.topic:default}")
    private String topic;
}
