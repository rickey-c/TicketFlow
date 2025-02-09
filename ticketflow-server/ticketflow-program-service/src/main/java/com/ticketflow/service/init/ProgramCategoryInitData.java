package com.ticketflow.service.init;

import com.ticketflow.BusinessThreadPool;
import com.ticketflow.base.AbstractApplicationPostConstructHandler;
import com.ticketflow.service.ProgramCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @Description: 节目redis缓存预热
 * @Author: rickey-c
 * @Date: 2025/2/9 14:33
 */
@Component
public class ProgramCategoryInitData extends AbstractApplicationPostConstructHandler {

    @Autowired
    private ProgramCategoryService programCategoryService;

    @Override
    public Integer executeOrder() {
        return 1;
    }

    @Override
    public void executeInit(ConfigurableApplicationContext context) {
        BusinessThreadPool.execute(() -> {
            programCategoryService.programCategoryRedisInit();
        });
    }
}
