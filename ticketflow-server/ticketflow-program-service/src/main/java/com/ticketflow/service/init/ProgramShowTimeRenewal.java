package com.ticketflow.service.init;

import com.ticketflow.base.AbstractApplicationPostConstructHandler;
import com.ticketflow.core.SpringUtil;
import com.ticketflow.service.ProgramService;
import com.ticketflow.service.ProgramShowTimeService;
import com.ticketflow.util.BusinessEsHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @Description: 节目演出时间更新
 * @Author: rickey-c
 * @Date: 2025/2/9 14:45
 */
@Component
public class ProgramShowTimeRenewal extends AbstractApplicationPostConstructHandler {

    @Autowired
    private ProgramShowTimeService programShowTimeService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private BusinessEsHandle businessEsHandle;

    @Override
    public Integer executeOrder() {
        return 2;
    }

    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        // 更新节目更新时间
        Set<Long> programIdSet = programShowTimeService.renewal();
        if (!programIdSet.isEmpty()) {
            businessEsHandle.deleteIndex(SpringUtil.getPrefixDistinctionName() + "-" +
                    ProgramDocumentParamName.INDEX_NAME);
            for (Long programId : programIdSet) {
                programService.delRedisData(programId);
                programService.delLocalCache(programId);
            }
        }
    }
}
