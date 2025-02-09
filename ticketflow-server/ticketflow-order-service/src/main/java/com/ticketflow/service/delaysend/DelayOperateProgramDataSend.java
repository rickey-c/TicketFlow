package com.ticketflow.service.delaysend;

import com.ticketflow.core.SpringUtil;
import com.ticketflow.context.DelayQueueContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ticketflow.service.constant.OrderConstant.*;

/**
 * @Description: 订单支付成功后 更新相关数据
 * @Author: rickey-c
 * @Date: 2025/2/9 17:41
 */
@Slf4j
@Component
public class DelayOperateProgramDataSend {

    @Autowired
    private DelayQueueContext delayQueueContext;

    public void sendMessage(String message) {
        try {
            delayQueueContext.sendMessage(SpringUtil.getPrefixDistinctionName() + "-" + DELAY_OPERATE_PROGRAM_DATA_TOPIC,
                    message, DELAY_OPERATE_PROGRAM_DATA_TIME, DELAY_OPERATE_PROGRAM_DATA_TIME_UNIT);
        } catch (Exception e) {
            log.error("send message error message : {}", message, e);
        }

    }
}
