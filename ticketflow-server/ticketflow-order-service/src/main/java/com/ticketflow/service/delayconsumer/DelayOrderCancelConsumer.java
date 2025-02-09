package com.ticketflow.service.delayconsumer;

import com.alibaba.fastjson.JSON;
import com.ticketflow.core.ConsumerTask;
import com.ticketflow.core.SpringUtil;
import com.ticketflow.dto.DelayOrderCancelDto;
import com.ticketflow.dto.OrderCancelDto;
import com.ticketflow.service.OrderService;
import com.ticketflow.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ticketflow.service.constant.OrderConstant.DELAY_ORDER_CANCEL_TOPIC;

/**
 * @Description: 延迟订单取消
 * @Author: rickey-c
 * @Date: 2025/2/9 20:54
 */
@Slf4j
@Component
public class DelayOrderCancelConsumer implements ConsumerTask {
    
    @Autowired
    private OrderService orderService;
    
    @Override
    public void execute(String content) {
        log.info("延迟订单取消消息进行消费 content : {}", content);
        if (StringUtil.isEmpty(content)) {
            log.error("延迟队列消息不存在");
            return;
        }
        DelayOrderCancelDto delayOrderCancelDto = JSON.parseObject(content, DelayOrderCancelDto.class);
        
        //取消订单
        OrderCancelDto orderCancelDto = new OrderCancelDto();
        orderCancelDto.setOrderNumber(delayOrderCancelDto.getOrderNumber());
        boolean cancel = orderService.cancel(orderCancelDto);
        if (cancel) {
            log.info("延迟订单取消成功 orderCancelDto : {}",content);
        }else {
            log.error("延迟订单取消失败 orderCancelDto : {}",content);
        }
    }
    
    @Override
    public String topic() {
        return SpringUtil.getPrefixDistinctionName() + "-" + DELAY_ORDER_CANCEL_TOPIC;
    }
}
