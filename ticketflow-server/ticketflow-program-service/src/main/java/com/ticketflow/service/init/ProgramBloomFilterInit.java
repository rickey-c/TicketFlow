package com.ticketflow.service.init;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketflow.base.AbstractApplicationPostConstructHandler;
import com.ticketflow.handler.BloomFilterHandler;
import com.ticketflow.service.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 布隆过滤器初始化
 * @Author: rickey-c
 * @Date: 2025/2/9 14:30
 */
@Component
public class ProgramBloomFilterInit extends AbstractApplicationPostConstructHandler {

    @Autowired
    private ProgramService programService;

    @Autowired
    private BloomFilterHandler bloomFilterHandler;

    @Override
    public Integer executeOrder() {
        return 4;
    }

    @Override
    public void executeInit(ConfigurableApplicationContext context) {
        List<Long> allProgramIdList = programService.getAllProgramIdList();
        if (CollectionUtil.isEmpty(allProgramIdList)) {
            return;
        }
        allProgramIdList.forEach(programId -> bloomFilterHandler.add(String.valueOf(programId)));
    }
}
