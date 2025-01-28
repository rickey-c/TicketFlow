package com.ticketflow.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.ticketflow.dto.AllRuleDto;
import com.ticketflow.dto.DepthRuleDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.RuleStatus;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.vo.AllDepthRuleVo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 所有规则服务
 * @Author: rickey-c
 * @Date: 2025/1/28 14:51
 */
@Service
@AllArgsConstructor
public class AllRuleService {
    
    private final RuleService ruleService;
    
    private final DepthRuleService depthRuleService;
    
    @Transactional(rollbackFor = Exception.class)
    public void add(AllRuleDto allRuleDto){
        ruleService.add(allRuleDto.getRuleDto());
        depthRuleService.delAll();
        List<DepthRuleDto> depthRuleDtos = allRuleDto.getDepthRuleDtoList();
        if (CollUtil.isNotEmpty(depthRuleDtos)){
            for (int i = 0; i < depthRuleDtos.size(); i++) {
                DepthRuleDto depthRuleDto = depthRuleDtos.get(i);
                checkTime(depthRuleDto.getStartTimeWindow(),depthRuleDto.getEndTimeWindow(),filterDepthRuleDtoList(depthRuleDtos,i));
                depthRuleService.add(depthRuleDto);
            }
        }
    }
    
    public AllDepthRuleVo get() {
        AllDepthRuleVo allDepthRuleVo = new AllDepthRuleVo();
        allDepthRuleVo.setRuleVo(ruleService.get());
        allDepthRuleVo.setDepthRuleVoList(depthRuleService.selectList());
        return allDepthRuleVo;
    }

    /**
     * 过滤掉下标为 i 的 depthRuleDto
     * @param depthRuleDtoList 深度规则 dto
     * @param currentIndex 当前下标 i
     * @return 过滤后的深度规则
     */
    public List<DepthRuleDto> filterDepthRuleDtoList(List<DepthRuleDto> depthRuleDtoList, int currentIndex){
        List<DepthRuleDto> fiterDepthRuleDtoList = new ArrayList<>();
        for (int i = 0; i < depthRuleDtoList.size(); i++) {
            if (i != currentIndex) {
                fiterDepthRuleDtoList.add(depthRuleDtoList.get(i));
            }
        }
        return fiterDepthRuleDtoList;
    }

    public void checkTime(@NotBlank String startTimeWindow, @NotBlank String endTimeWindow, List<DepthRuleDto> depthRuleDtos) {
        // 过滤得到状态是正常的规则
        depthRuleDtos=depthRuleDtos.stream().filter(depthRuleDto -> {
            if (depthRuleDto.getStatus()!=null){
                return depthRuleDto.getStatus().equals(RuleStatus.RUN.getCode());   
            }else {
                return false;
            }
        }).collect(Collectors.toList());
        // 如果窗口重叠，就抛出异常
        for (final DepthRuleDto depthRuleDto : depthRuleDtos) {
            long checkStartTimeWindowTimestamp = getTimeWindowTimestamp(startTimeWindow);
            long checkEndTimeWindowTimestamp = getTimeWindowTimestamp(endTimeWindow);
            long startTimeWindowTimestamp = getTimeWindowTimestamp(depthRuleDto.getStartTimeWindow());
            long endTimeWindowTimestamp = getTimeWindowTimestamp(depthRuleDto.getEndTimeWindow());
            boolean checkStartLimitTimeResult = checkStartTimeWindowTimestamp >= startTimeWindowTimestamp && checkStartTimeWindowTimestamp <= endTimeWindowTimestamp;
            boolean checkEndLimitTimeResult = checkEndTimeWindowTimestamp >= startTimeWindowTimestamp && checkEndTimeWindowTimestamp <= endTimeWindowTimestamp;
            if (checkStartLimitTimeResult || checkEndLimitTimeResult) {
                throw new TicketFlowFrameException(BaseCode.API_RULE_TIME_WINDOW_INTERSECT);
            }
        }
        
    }

    public long getTimeWindowTimestamp(String timeWindow){
        String today = DateUtil.today();
        return DateUtil.parse(today + " " + timeWindow).getTime();
    }

    
}
