package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.dto.DepthRuleDto;
import com.ticketflow.dto.DepthRuleStatusDto;
import com.ticketflow.dto.DepthRuleUpdateDto;
import com.ticketflow.entity.DepthRule;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.RuleStatus;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.mapper.DepthRuleMapper;
import com.ticketflow.utils.DateUtils;
import com.ticketflow.vo.DepthRuleVo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 深度规则服务
 * @Author: rickey-c
 * @Date: 2025/1/28 14:53
 */
@Service
@AllArgsConstructor
public class DepthRuleService {

    private final DepthRuleMapper depthRuleMapper;

    private final RuleService ruleService;
    
    private final UidGenerator uidGenerator;

    @Transactional(rollbackFor = Exception.class)
    public void depthRuleAdd(DepthRuleDto depthRuleDto) {
        // 时间段是否是子集，如果是，则抛出异常
        check(depthRuleDto.getStartTimeWindow(), depthRuleDto.getEndTimeWindow());
        add(depthRuleDto);
        ruleService.saveAllRuleCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(DepthRuleDto depthRuleDto) {
        DepthRule depthRule = new DepthRule();
        BeanUtil.copyProperties(depthRuleDto,depthRule);
        depthRule.setId(uidGenerator.getUid());
        depthRule.setCreateTime(DateUtils.now());
        depthRuleMapper.insert(depthRule);
    }

    public void check(@NotBlank String startTimeWindow,@NotBlank String endTimeWindow) {
        LambdaQueryWrapper<DepthRule> wrapper = Wrappers.lambdaQuery(DepthRule.class).eq(DepthRule::getStatus, RuleStatus.RUN.getCode());
        List<DepthRule> depthRules = depthRuleMapper.selectList(wrapper);
        for (DepthRule depthRule : depthRules) {
            long checkStartTimeWindowTimestamp = getTimeWindowTimestamp(startTimeWindow);
            long checkEndTimeWindowTimestamp = getTimeWindowTimestamp(endTimeWindow);
            long startTimeWindowTimestamp = getTimeWindowTimestamp(depthRule.getStartTimeWindow());
            long endTimeWindowTimestamp = getTimeWindowTimestamp(depthRule.getEndTimeWindow());
            boolean checkStartLimitTimeResult = checkStartTimeWindowTimestamp >= startTimeWindowTimestamp 
                    && checkStartTimeWindowTimestamp <= endTimeWindowTimestamp;
            boolean checkEndLimitTimeResult = checkEndTimeWindowTimestamp >= startTimeWindowTimestamp 
                    && checkEndTimeWindowTimestamp <= endTimeWindowTimestamp;
            if (!checkStartLimitTimeResult || !checkEndLimitTimeResult){
                throw new TicketFlowFrameException(BaseCode.API_RULE_TIME_WINDOW_INTERSECT);
            }
        }
    }
    
    public long getTimeWindowTimestamp(String timeWindow){
        String today = DateUtil.today();
        return DateUtil.parse(today + " " + timeWindow).getTime();
    }

    @Transactional(rollbackFor = Exception.class)
    public void depthRuleUpdate(final DepthRuleUpdateDto depthRuleUpdateDto) {
        DepthRule depthRule = new DepthRule();
        BeanUtils.copyProperties(depthRuleUpdateDto,depthRule);
        depthRuleMapper.updateById(depthRule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void depthRuleUpdateStatus(final DepthRuleStatusDto depthRuleStatusDto) {
        updateStatus(depthRuleStatusDto);
        ruleService.saveAllRuleCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(final DepthRuleStatusDto depthRuleStatusDto) {
        DepthRule depthRule = new DepthRule();
        depthRule.setId(depthRuleStatusDto.getId());
        depthRule.setStatus(depthRuleStatusDto.getStatus());
        depthRuleMapper.updateById(depthRule);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<DepthRuleVo> selectList(){
        List<DepthRule> depthRules = depthRuleMapper.selectList(null);
        return depthRules.stream().map(depthRule -> {
            DepthRuleVo depthRuleVo = new DepthRuleVo();
            BeanUtil.copyProperties(depthRule, depthRuleVo);
            return depthRuleVo;
        }).collect(Collectors.toList());
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void delAll(){
        depthRuleMapper.delAll();
    }
}
