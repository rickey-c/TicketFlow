package com.ticketflow.service.kafka;

import com.ticketflow.core.SpringUtil;
import com.ticketflow.mq.callback.FailureCallback;
import com.ticketflow.mq.callback.SuccessCallback;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @Description: kafka 创建订单消息发送
 * @Author: rickey-c
 * @Date: 2025/2/7 22:25
 */
@Slf4j
@AllArgsConstructor
@Component
public class CreateOrderSend {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTopic kafkaTopic;

    public void sendMessage(String message, SuccessCallback<SendResult<String, String>> successCallback,
                            FailureCallback failureCallback) {
        log.info("创建订单kafka发送消息 消息体 : {}", message);
        CompletableFuture<SendResult<String, String>> completableFuture =
                kafkaTemplate.send(SpringUtil.getPrefixDistinctionName() + "-" + kafkaTopic.getTopic(), message);
        completableFuture.whenComplete((result, ex) -> {
            if (Objects.isNull(ex)) {
                successCallback.onSuccess(result);
            } else {
                failureCallback.onFailure(ex);
            }
        });
    }
}
