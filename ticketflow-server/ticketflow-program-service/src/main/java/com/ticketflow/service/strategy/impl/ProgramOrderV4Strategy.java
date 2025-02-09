package com.ticketflow.service.strategy.impl;

import com.ticketflow.base.AbstractApplicationCommandLineRunnerHandler;
import com.ticketflow.core.RepeatExecuteLimitConstants;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.enums.ProgramOrderVersion;
import com.ticketflow.impl.composite.CompositeContainer;
import com.ticketflow.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketflow.service.ProgramOrderService;
import com.ticketflow.service.strategy.BaseProgramOrder;
import com.ticketflow.service.strategy.ProgramOrderContext;
import com.ticketflow.service.strategy.ProgramOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static com.ticketflow.core.DistributedLockConstants.PROGRAM_ORDER_CREATE_V4;

/**
 * @Description: 节目订单 v4
 * @Author: rickey-c
 * @Date: 2025/2/9 15:25
 */
@Slf4j
@Component
public class ProgramOrderV4Strategy extends AbstractApplicationCommandLineRunnerHandler implements ProgramOrderStrategy {

    @Autowired
    private ProgramOrderService programOrderService;

    @Autowired
    private BaseProgramOrder baseProgramOrder;

    @Autowired
    private CompositeContainer compositeContainer;

    @RepeatExecuteLimit(
            name = RepeatExecuteLimitConstants.CREATE_PROGRAM_ORDER,
            keys = {"#programOrderCreateDto.userId", "#programOrderCreateDto.programId"})
    @Override
    public String createOrder(ProgramOrderCreateDto programOrderCreateDto) {
        compositeContainer.execute(CompositeCheckType.PROGRAM_ORDER_CREATE_CHECK.getValue(), programOrderCreateDto);
        return baseProgramOrder.localLockCreateOrder(PROGRAM_ORDER_CREATE_V4, programOrderCreateDto,
                () -> programOrderService.createNewAsync(programOrderCreateDto));
    }

    @Override
    public Integer executeOrder() {
        return 4;
    }

    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        ProgramOrderContext.add(ProgramOrderVersion.V4_VERSION.getVersion(), this);
    }
}
