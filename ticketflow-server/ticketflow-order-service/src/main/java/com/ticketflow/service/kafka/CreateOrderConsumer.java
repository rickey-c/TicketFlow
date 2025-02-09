package com.ticketflow.service.kafka;

import com.alibaba.fastjson.JSON;
import com.ticketflow.dto.OrderCreateDto;
import com.ticketflow.dto.OrderTicketUserCreateDto;
import com.ticketflow.enums.OrderStatus;
import com.ticketflow.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.damai.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;

/**
 * @Description: kafka创建订单消费
 * @Author: rickey-c
 * @Date: 2025/2/9 22:23
 */
@Slf4j
@AllArgsConstructor
@Component
public class CreateOrderConsumer {

    @Autowired
    private OrderService orderService;

    public static Long MESSAGE_DELAY_TIME = 5000L;

    @KafkaListener(topics = {SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"${spring.kafka.topic:create_order}"})
    public void consumerOrderMessage(ConsumerRecord<String,String> consumerRecord){
        try {
            Optional.ofNullable(consumerRecord.value()).map(String::valueOf).ifPresent(value -> {

                OrderCreateDto orderCreateDto = JSON.parseObject(value, OrderCreateDto.class);

                long createOrderTimeTimestamp = orderCreateDto.getCreateOrderTime().getTime();

                long currentTimeTimestamp = System.currentTimeMillis();

                long delayTime = currentTimeTimestamp - createOrderTimeTimestamp;

                log.info("消费到kafka的创建订单消息 消息体: {} 延迟时间 : {} 毫秒",value,delayTime);
                //如果消费到消息时，延迟时间超过了5s，那么此订单丢弃，将库存回滚回去
                if (currentTimeTimestamp - createOrderTimeTimestamp > MESSAGE_DELAY_TIME) {
                    log.info("消费到kafka的创建订单消息延迟时间大于了 {} 毫秒 此订单消息被丢弃 订单号 : {}",
                            delayTime,orderCreateDto.getOrderNumber());
                    Map<Long, List<OrderTicketUserCreateDto>> orderTicketUserSeatList =
                            orderCreateDto.getOrderTicketUserCreateDtoList().stream().collect(Collectors.groupingBy(OrderTicketUserCreateDto::getTicketCategoryId));
                    Map<Long,List<Long>> seatMap = new HashMap<>(orderTicketUserSeatList.size());
                    orderTicketUserSeatList.forEach((k,v) -> {
                        seatMap.put(k,v.stream().map(OrderTicketUserCreateDto::getSeatId).collect(Collectors.toList()));
                    });
                    //数据恢复
                    orderService.updateProgramRelatedDataMq(orderCreateDto.getProgramId(),seatMap, OrderStatus.CANCEL);
                }else {
                    String orderNumber = orderService.createMq(orderCreateDto);
                    log.info("消费到kafka的创建订单消息 创建订单成功 订单号 : {}",orderNumber);
                }
            });
        }catch (Exception e) {
            log.error("处理消费到kafka的创建订单消息失败 error",e);
        }
    }
}
