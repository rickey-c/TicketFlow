package com.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @Description: 全部规则dto
 * @Author: rickey-c
 * @Date: 2025/1/24 15:32
 */
@Data
@Schema(title="AllRuleDto", description ="全部规则")
public class AllRuleDto {
    
    @Schema(name ="ruleDto", type ="RuleDto", description ="普通规则", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private RuleDto ruleDto;
    
    @Schema(name ="depthRuleDtoList", type ="DepthRuleDto[]", description ="深度规则")
    private List<DepthRuleDto> depthRuleDtoList;
}
