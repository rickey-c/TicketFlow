package com.ticketflow.service.scheduletask;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketflow.BusinessThreadPool;
import com.ticketflow.dto.ProgramResetExecuteDto;
import com.ticketflow.service.ProgramService;
import com.ticketflow.service.init.ProgramElasticsearchInitData;
import com.ticketflow.service.init.ProgramShowTimeRenewal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 定时任务-重置数据
 * @Author: rickey-c
 * @Date: 2025/2/9 15:09
 */
@Slf4j
@Component
public class ProgramDataTask {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramShowTimeRenewal programShowTimeRenewal;

    @Autowired
    private ProgramElasticsearchInitData programElasticsearchInitData;

    @Scheduled(cron = "0 0 23 * * ?")
    public void executeTask() {
        BusinessThreadPool.execute(() -> {
            try {
                log.warn("定时任务重置执行");
                List<Long> allProgramIdList = programService.getAllProgramIdList();
                if (CollectionUtil.isNotEmpty(allProgramIdList)) {
                    for (Long programId : allProgramIdList) {
                        ProgramResetExecuteDto programResetExecuteDto = new ProgramResetExecuteDto();
                        programResetExecuteDto.setProgramId(programId);
                        programService.resetExecute(programResetExecuteDto);
                    }
                }
                programShowTimeRenewal.executeInit(applicationContext);
                programElasticsearchInitData.executeInit(applicationContext);

            } catch (Exception e) {
                log.error("executeTask error", e);
            }
        });
    }
}
