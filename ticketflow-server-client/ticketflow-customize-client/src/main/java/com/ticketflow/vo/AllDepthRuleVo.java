package com.ticketflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Description: 所有规则vo
 * @Author: rickey-c
 * @Date: 2025/1/25 21:44
 */
@Data
@Schema(title="AllDepthRuleVo", description ="全部规则")
public class AllDepthRuleVo {
    
    @Schema(name ="ruleDto", type ="RuleDto", description ="普通规则")
    private RuleVo ruleVo;
    
    @Schema(name ="depthRuleDtoList", type ="DepthRuleDto[]", description ="深度规则")
    private List<DepthRuleVo> depthRuleVoList;
}
