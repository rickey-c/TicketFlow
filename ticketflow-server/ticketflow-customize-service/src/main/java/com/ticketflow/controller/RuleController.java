package com.ticketflow.controller;


import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.RuleDto;
import com.ticketflow.dto.RuleGetDto;
import com.ticketflow.dto.RuleStatusDto;
import com.ticketflow.dto.RuleUpdateDto;
import com.ticketflow.service.RuleService;
import com.ticketflow.vo.RuleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 规则调用控制层
 * @Author: rickey-c
 * @Date: 2025/1/29 2:01
 */
@RestController
@RequestMapping("/rule")
@Tag(name = "rule", description = "规则")
@AllArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @Operation(summary = "添加普通规则")
    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody RuleDto ruleDto) {
        ruleService.ruleAdd(ruleDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "修改普通规则")
    @PostMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody RuleUpdateDto ruleUpdateDto) {
        ruleService.ruleUpdate(ruleUpdateDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "修改普通规则状态")
    @PostMapping("/updateStatus")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody RuleStatusDto ruleStatusDto) {
        ruleService.ruleUpdateStatus(ruleStatusDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "查询普通规则")
    @PostMapping("/get")
    public ApiResponse<RuleVo> get(@Valid @RequestBody RuleGetDto ruleGetDto) {
        return ApiResponse.ok(ruleService.get(ruleGetDto));
    }
}
