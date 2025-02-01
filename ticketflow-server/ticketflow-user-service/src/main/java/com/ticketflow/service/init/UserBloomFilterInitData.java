package com.ticketflow.service.init;

import cn.hutool.core.collection.CollectionUtil;
import com.ticketflow.BusinessThreadPool;
import com.ticketflow.base.AbstractApplicationPostConstructHandler;
import com.ticketflow.handler.BloomFilterHandler;
import com.ticketflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 布隆过滤器初始化
 * @Author: rickey-c
 * @Date: 2025/2/1 16:38
 */
@Component
public class UserBloomFilterInitData extends AbstractApplicationPostConstructHandler {

    @Autowired
    private BloomFilterHandler bloomFilterHandler;

    @Autowired
    private UserService userService;

    @Override
    public Integer executeOrder() {
        return 1;
    }

    @Override
    public void executeInit(ConfigurableApplicationContext context) {
        BusinessThreadPool.execute(() -> {
            List<String> allMobile = userService.getAllMobile();
            if (CollectionUtil.isNotEmpty(allMobile)) {
                allMobile.forEach(mobile -> bloomFilterHandler.add(mobile));
            }
        });
    }
}
