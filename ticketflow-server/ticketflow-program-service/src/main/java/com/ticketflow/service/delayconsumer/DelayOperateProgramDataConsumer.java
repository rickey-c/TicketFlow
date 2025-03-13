package com.ticketflow.service.delayconsumer;

import com.alibaba.fastjson.JSON;
import com.ticketflow.core.SpringUtil;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.core.ConsumerTask;
import com.ticketflow.dto.ProgramOperateDataDto;
import com.ticketflow.service.ProgramService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ticketflow.service.constant.ProgramOrderConstant.DELAY_OPERATE_PROGRAM_DATA_TOPIC;

/**
 * @Description: 延迟消费监听
 * @Author: rickey-c
 * @Date: 2025/2/7 21:19
 */
@Slf4j
@Component
public class DelayOperateProgramDataConsumer implements ConsumerTask {

    @Autowired
    private ProgramService programService;

    @Override
    public void execute(String content) {
        log.info("延迟操作节目数据消息进行消费 content : {}", content);
        if (StringUtil.isEmpty(content)) {
            log.error("延迟队列消息不存在");
            return;
        }
        ProgramOperateDataDto programOperateDataDto = JSON.parseObject(content, ProgramOperateDataDto.class);
        programService.operateProgramData(programOperateDataDto);
    }

    @Override
    public String topic() {
        return SpringUtil.getPrefixDistinctionName() + "-" + DELAY_OPERATE_PROGRAM_DATA_TOPIC;
    }
}
