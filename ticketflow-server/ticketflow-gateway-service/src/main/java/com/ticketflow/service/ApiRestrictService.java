package com.ticketflow.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.fsg.uid.UidGenerator;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.ApiDataDto;
import com.ticketflow.enums.ApiRuleType;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.RuleTimeUnit;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.kafka.ApiDataMessageSend;
import com.ticketflow.property.GatewayProperty;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.service.lua.ApiRestrictCacheOperate;
import com.ticketflow.utils.DateUtils;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.DepthRuleVo;
import com.ticketflow.vo.RuleVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/29 20:51
 */
@Slf4j
@Component
public class ApiRestrictService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private GatewayProperty gatewayProperty;

    @Autowired
    private ApiDataMessageSend apiDataMessageSend;

    @Autowired
    private ApiRestrictCacheOperate apiRestrictCacheOperate;

    @Autowired
    private UidGenerator uidGenerator;

    /**
     * 检查请求路径是否被限制
     *
     * @param requestUri 请求路径
     * @return api调用结果是否被限制
     */
    public boolean checkApiRestrict(String requestUri) {
        if (gatewayProperty.getApiRestrictPaths() != null) {
            for (String apiRestrictPath : gatewayProperty.getApiRestrictPaths()) {
                PathMatcher matcher = new AntPathMatcher();
                if (matcher.match(apiRestrictPath, requestUri)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void apiRestrict(String id, String url, ServerHttpRequest request) {
        if (!checkApiRestrict(url)) {
            return;
        }
        long triggerResult = 0;
        long triggerCallStat = 0;
        long apiCount;
        long threshold;
        long messageIndex;
        String message = "";
        String ip = getIpAddress(request);
        String commonKey = ip + "_" + (id != null ? id + "_" : "") + url; // 构建唯一请求标识

        try {
            List<DepthRuleVo> depthRuleVos = new ArrayList<>();
            // 获取普通规则
            RuleVo ruleVo = redisCache.getForHash(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.ALL_RULE_HASH),
                    RedisKeyBuild.createRedisKey(RedisKeyManage.RULE).getRelKey(),
                    RuleVo.class
            );
            String depthRuleStr = redisCache.getForHash(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.ALL_RULE_HASH),
                    RedisKeyBuild.createRedisKey(RedisKeyManage.DEPTH_RULE).getRelKey(),
                    String.class
            );
            if ((StringUtil.isNotEmpty(depthRuleStr))) {
                depthRuleVos = JSON.parseArray(depthRuleStr, DepthRuleVo.class);
            }
            int apiRuleType = ApiRuleType.NO_RULE.getCode(); // 规则类型：0 无规则，1 普通规则，2 深度规则
            if (ruleVo != null) {
                apiRuleType = ApiRuleType.RULE.getCode();
                message = ruleVo.getMessage();
            }
            if (ruleVo != null && CollectionUtil.isNotEmpty(depthRuleVos)) {
                apiRuleType = ApiRuleType.DEPTH_RULE.getCode();
            }

            if (apiRuleType == ApiRuleType.RULE.getCode()
                    || apiRuleType == ApiRuleType.DEPTH_RULE.getCode()) {
                // 构建普通规则参数
                JSONObject parameter = getRuleParameter(apiRuleType, commonKey, ruleVo);

                // 构建深度规则参数
                if (apiRuleType == ApiRuleType.DEPTH_RULE.getCode()) {
                    parameter = getDepthRuleParameter(parameter, commonKey, depthRuleVos);
                }
                ApiRestrictData apiRestrictData = apiRestrictCacheOperate.apiRuleOperate(
                        Collections.singletonList(JSON.toJSONString(parameter)), new Object[]{}
                );
                // 获取限流结果
                triggerResult = apiRestrictData.getTriggerResult(); // 是否触发规则
                triggerCallStat = apiRestrictData.getTriggerCallStat(); // 是否保存记录
                apiCount = apiRestrictData.getApiCount(); // 当前请求数
                threshold = apiRestrictData.getThreshold(); // 阈值
                messageIndex = apiRestrictData.getMessageIndex(); // 提示信息索引
                if (messageIndex != -1) {
                    message = Optional.ofNullable(depthRuleVos.get((int) messageIndex))
                            .map(DepthRuleVo::getMessage)
                            .orElse(message);
                }
                log.info("API限流结果 [key: {}], [触发: {}], [保存记录: {}], [请求数: {}], [阈值: {}]",
                        commonKey, triggerResult, triggerCallStat, apiCount, threshold);
            }
        } catch (Exception e) {
            log.error("Redis lua 脚本执行错误", e);
        }

        if (triggerResult == 1) {
            if (triggerCallStat == ApiRuleType.RULE.getCode()
                    || triggerCallStat == ApiRuleType.DEPTH_RULE.getCode()) {
                saveApiData(request, url, (int) triggerCallStat); // 保存请求记录
            }
            String defaultMessage = BaseCode.API_RULE_TRIGGER.getMsg();
            if (StringUtil.isNotEmpty(message)) {
                defaultMessage = message;
            }
            throw new TicketFlowFrameException(BaseCode.API_RULE_TRIGGER.getCode(), defaultMessage);
        }

    }


    public static String getIpAddress(ServerHttpRequest request) {
        String unknown = "unknown";
        String split = ",";
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && !ip.isEmpty() && !unknown.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(split)) {
                ip = ip.split(split)[0];
            }
        }
        if (ip == null || ip.isEmpty() || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || unknown.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || unknown.equalsIgnoreCase(ip)) {
            ip = Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
        }
        return ip;
    }


    public JSONObject getRuleParameter(int apiRuleType, String commonKey, RuleVo ruleVo) {
        JSONObject parameter = new JSONObject();

        parameter.put("apiRuleType", apiRuleType);

        String ruleKey = "rule_api_limit" + "_" + commonKey;
        parameter.put("ruleKey", ruleKey);

        parameter.put("statTime", String.valueOf(Objects.equals(ruleVo.getStatTimeType(), RuleTimeUnit.SECOND.getCode()) ? ruleVo.getStatTime() : ruleVo.getStatTime() * 60));

        parameter.put("threshold", ruleVo.getThreshold());

        parameter.put("effectiveTime", String.valueOf(Objects.equals(ruleVo.getEffectiveTimeType(), RuleTimeUnit.SECOND.getCode()) ? ruleVo.getEffectiveTime() : ruleVo.getEffectiveTime() * 60));

        parameter.put("ruleLimitKey", RedisKeyBuild.createRedisKey(RedisKeyManage.RULE_LIMIT, commonKey).getRelKey());

        parameter.put("zSetRuleStatKey", RedisKeyBuild.createRedisKey(RedisKeyManage.Z_SET_RULE_STAT, commonKey).getRelKey());

        return parameter;
    }

    public JSONObject getDepthRuleParameter(JSONObject parameter, String commonKey, List<DepthRuleVo> depthRuleVoList) {
        depthRuleVoList = sortStartTimeWindow(depthRuleVoList);

        parameter.put("depthRuleSize", String.valueOf(depthRuleVoList.size()));

        parameter.put("currentTime", System.currentTimeMillis());

        List<JSONObject> depthRules = new ArrayList<>();
        for (int i = 0; i < depthRuleVoList.size(); i++) {
            JSONObject depthRule = new JSONObject();
            DepthRuleVo depthRuleVo = depthRuleVoList.get(i);

            depthRule.put("statTime", Objects.equals(depthRuleVo.getStatTimeType(), RuleTimeUnit.SECOND.getCode()) ? depthRuleVo.getStatTime() : depthRuleVo.getStatTime() * 60);

            depthRule.put("threshold", depthRuleVo.getThreshold());

            depthRule.put("effectiveTime", String.valueOf(Objects.equals(depthRuleVo.getEffectiveTimeType(), RuleTimeUnit.SECOND.getCode()) ? depthRuleVo.getEffectiveTime() : depthRuleVo.getEffectiveTime() * 60));

            depthRule.put("depthRuleLimit", RedisKeyBuild.createRedisKey(RedisKeyManage.DEPTH_RULE_LIMIT, i, commonKey).getRelKey());

            depthRule.put("startTimeWindowTimestamp", depthRuleVo.getStartTimeWindowTimestamp());
            depthRule.put("endTimeWindowTimestamp", depthRuleVo.getEndTimeWindowTimestamp());

            depthRules.add(depthRule);
        }

        parameter.put("depthRules", depthRules);

        return parameter;
    }

    public List<DepthRuleVo> sortStartTimeWindow(List<DepthRuleVo> depthRuleVoList) {
        return depthRuleVoList.stream().peek(depthRuleVo -> {
            depthRuleVo.setStartTimeWindowTimestamp(getTimeWindowTimestamp(depthRuleVo.getStartTimeWindow()));
            depthRuleVo.setEndTimeWindowTimestamp((getTimeWindowTimestamp(depthRuleVo.getEndTimeWindow())));
        }).sorted(Comparator.comparing(DepthRuleVo::getStartTimeWindowTimestamp)).collect(Collectors.toList());
    }

    public long getTimeWindowTimestamp(String timeWindow) {
        String today = DateUtil.today();
        return DateUtil.parse(today + " " + timeWindow).getTime();
    }

    public void saveApiData(org.springframework.http.server.reactive.ServerHttpRequest request, String apiUrl, Integer type) {
        ApiDataDto apiDataDto = new ApiDataDto();
        apiDataDto.setId(uidGenerator.getUid());
        apiDataDto.setApiAddress(getIpAddress(request));
        apiDataDto.setApiUrl(apiUrl);
        apiDataDto.setCreateTime(DateUtils.now());
        apiDataDto.setCallDayTime(DateUtils.nowStr(DateUtils.FORMAT_DATE));
        apiDataDto.setCallHourTime(DateUtils.nowStr(DateUtils.FORMAT_HOUR));
        apiDataDto.setCallMinuteTime(DateUtils.nowStr(DateUtils.FORMAT_MINUTE));
        apiDataDto.setCallSecondTime(DateUtils.nowStr(DateUtils.FORMAT_SECOND));
        apiDataDto.setType(type);
        Optional.ofNullable(apiDataMessageSend).ifPresent(send -> send.sendMessage(JSON.toJSONString(apiDataDto)));
    }

}
