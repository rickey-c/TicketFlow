package com.ticketflow.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.RuleDto;
import com.ticketflow.dto.RuleGetDto;
import com.ticketflow.dto.RuleStatusDto;
import com.ticketflow.dto.RuleUpdateDto;
import com.ticketflow.entity.DepthRule;
import com.ticketflow.entity.Rule;
import com.ticketflow.enums.RuleStatus;
import com.ticketflow.mapper.DepthRuleMapper;
import com.ticketflow.mapper.RuleMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.vo.RuleVo;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: 普通规则Service
 * @Author: rickey-c
 * @Date: 2025/1/28 14:52
 */
@Service
@AllArgsConstructor
public class RuleService {

    private final RuleMapper ruleMapper;

    private final RedisCache redisCache;

    private final DepthRuleMapper depthRuleMapper;

    private final UidGenerator uidGenerator;

    @Transactional(rollbackFor = Exception.class)
    public void ruleAdd(RuleDto ruleDto) {
        add(ruleDto);
        saveAllRuleCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long add(RuleDto ruleDto) {
        delAll();
        Rule rule = new Rule();
        BeanUtils.copyProperties(ruleDto, rule);
        rule.setId(uidGenerator.getId());
        rule.setCreateTime(DateUtil.date());
        ruleMapper.insert(rule);
        return rule.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void ruleUpdate(RuleUpdateDto ruleUpdateDto) {
        update(ruleUpdateDto);
        saveAllRuleCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(RuleUpdateDto ruleUpdateDto) {
        Rule rule = new Rule();
        BeanUtils.copyProperties(rule, ruleUpdateDto);
        ruleMapper.updateById(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void ruleUpdateStatus(RuleStatusDto ruleStatusDto) {
        updateStatus(ruleStatusDto);
        saveAllRuleCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(@NotNull RuleStatusDto ruleStatusDto) {
        Rule rule = new Rule();
        rule.setId(ruleStatusDto.getId());
        rule.setStatus(ruleStatusDto.getStatus());
        ruleMapper.updateById(rule);
    }

    public RuleVo get(RuleGetDto ruleGetDto) {
        RuleVo ruleVo = new RuleVo();
        Optional.ofNullable(ruleMapper.selectById(ruleGetDto.getId())).ifPresent(
                rule -> {
                    BeanUtils.copyProperties(rule, ruleVo);
                }
        );
        return ruleVo;
    }

    public RuleVo get() {
        RuleVo ruleVo = new RuleVo();
        Optional.ofNullable(ruleMapper.selectOne(null)).ifPresent(
                rule -> {
                    BeanUtils.copyProperties(rule, ruleVo);
                }
        );
        return ruleVo;
    }

    public void delAll() {
        ruleMapper.delAll();
    }

    public void saveAllRuleCache() {
        // 从数据库查询存在的规则
        Map<String, Object> map = new HashMap<>(2);
        LambdaQueryWrapper<Rule> ruleQueryWrapper = Wrappers.lambdaQuery(Rule.class).eq(Rule::getStatus, RuleStatus.RUN.getCode());
        Rule rule = ruleMapper.selectOne(ruleQueryWrapper);
        if (Optional.ofNullable(rule).isPresent()) {
            // 构造<key,value>
            map.put(RedisKeyBuild.createRedisKey(RedisKeyManage.RULE).getRelKey(), rule);
        }
        // 从数据库查询深度规则
        LambdaQueryWrapper<DepthRule> depthRuleQueryWrapper = Wrappers.lambdaQuery(DepthRule.class).eq(DepthRule::getStatus, RuleStatus.RUN.getCode());
        List<DepthRule> depthRules = depthRuleMapper.selectList(depthRuleQueryWrapper);
        if (CollUtil.isNotEmpty(depthRules)) {
            // 构造<key,value>
            map.put(RedisKeyBuild.createRedisKey(RedisKeyManage.DEPTH_RULE).getRelKey(), depthRules);
        }
        // 删除现有规则
        redisCache.del(RedisKeyBuild.createRedisKey(RedisKeyManage.ALL_RULE_HASH));
        if (!map.isEmpty() && Objects.nonNull(map.get(RedisKeyBuild.createRedisKey(RedisKeyManage.RULE).getRelKey()))) {
            // 添加新规则
            redisCache.putHash(RedisKeyBuild.createRedisKey(RedisKeyManage.ALL_RULE_HASH), map);
        }
    }
}
    