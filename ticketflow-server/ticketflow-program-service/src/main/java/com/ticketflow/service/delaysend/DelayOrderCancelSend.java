package com.ticketflow.service.delaysend;

import com.damai.core.SpringUtil;
import com.ticketflow.context.DelayQueueContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ticketflow.service.constant.ProgramOrderConstant.*;

/**
 * @Description: 延迟消息发送
 * @Author: rickey-c
 * @Date: 2025/2/7 21:21
 */
@Slf4j
@Component
public class DelayOrderCancelSend {

    @Autowired
    private DelayQueueContext delayQueueContext;

    public void sendMessage(String message) {
        try {
            log.info("延迟订单取消消息进行发送 消息体 : {}", message);
            delayQueueContext.sendMessage(SpringUtil.getPrefixDistinctionName() + "-" + DELAY_ORDER_CANCEL_TOPIC,
                    message, DELAY_ORDER_CANCEL_TIME, DELAY_ORDER_CANCEL_TIME_UNIT);
        } catch (Exception e) {
            log.error("send message error message : {}", message, e);
        }

    }
}
