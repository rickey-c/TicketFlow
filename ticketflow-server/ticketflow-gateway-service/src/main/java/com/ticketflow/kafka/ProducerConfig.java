package com.ticketflow.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @Description: kafka生产者自动配置
 * @Author: rickey-c
 * @Date: 2025/1/29 20:56
 */
@Configuration
@ConditionalOnProperty(value = "spring.kafka.bootstrap-servers")
public class ProducerConfig {

    @Bean
    public KafkaTopic kafkaTopic() {
        return new KafkaTopic();
    }

    @Bean
    public ApiDataMessageSend apiDataMessageSend(KafkaTemplate<String, String> kafkaTemplate, KafkaTopic kafkaTopic) {
        return new ApiDataMessageSend(kafkaTemplate, kafkaTopic.getTopic());
    }
}
