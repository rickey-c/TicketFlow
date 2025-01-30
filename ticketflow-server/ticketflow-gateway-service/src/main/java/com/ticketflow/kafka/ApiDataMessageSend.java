package com.ticketflow.kafka;

import com.ticketflow.core.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @Description: 消息发送
 * @Author: rickey-c
 * @Date: 2025/1/29 20:56
 */
@Slf4j
@AllArgsConstructor
public class ApiDataMessageSend {

    private KafkaTemplate<String, String> kafkaTemplate;

    private String topic;

    public void sendMessage(String message) {
        log.info("sendMessage message : {}", message);
        kafkaTemplate.send(SpringUtil.getPrefixDistinctionName() + "-" + topic, message);
    }

}
