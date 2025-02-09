package com.ticketflow.service.strategy.impl;

import com.ticketflow.enums.ProgramOrderVersion;
import com.ticketflow.base.AbstractApplicationCommandLineRunnerHandler;
import com.ticketflow.core.RepeatExecuteLimitConstants;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.impl.composite.CompositeContainer;
import com.ticketflow.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketflow.service.ProgramOrderService;
import com.ticketflow.service.strategy.ProgramOrderContext;
import com.ticketflow.service.strategy.ProgramOrderStrategy;
import com.ticketflow.servicelock.annotion.ServiceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static com.ticketflow.core.DistributedLockConstants.PROGRAM_ORDER_CREATE_V1;

/**
 * @Description: 节目订单 v1
 * @Author: rickey-c
 * @Date: 2025/2/9 15:25
 */
@Component
public class ProgramOrderV1Strategy extends AbstractApplicationCommandLineRunnerHandler implements ProgramOrderStrategy {

    @Autowired
    private ProgramOrderService programOrderService;

    @Autowired
    private CompositeContainer compositeContainer;


    @RepeatExecuteLimit(
            name = RepeatExecuteLimitConstants.CREATE_PROGRAM_ORDER,
            keys = {"#programOrderCreateDto.userId", "#programOrderCreateDto.programId"})
    @ServiceLock(name = PROGRAM_ORDER_CREATE_V1, keys = {"#programOrderCreateDto.programId"})
    @Override
    public String createOrder(final ProgramOrderCreateDto programOrderCreateDto) {
        // 组合模式进行校验
        compositeContainer.execute(CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue(), programOrderCreateDto);
        // 传播订单
        return programOrderService.create(programOrderCreateDto);
    }

    @Override
    public Integer executeOrder() {
        return 1;
    }

    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        ProgramOrderContext.add(ProgramOrderVersion.V1_VERSION.getVersion(), this);
    }
}
