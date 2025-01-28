package com.ticketflow.controller;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.DepthRuleDto;
import com.ticketflow.dto.DepthRuleStatusDto;
import com.ticketflow.dto.DepthRuleUpdateDto;
import com.ticketflow.service.DepthRuleService;
import com.ticketflow.vo.DepthRuleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 深度规则调用控制层
 * @Author: rickey-c
 * @Date: 2025/1/29 2:01
 */
@RestController
@RequestMapping("/depthRule")
@Tag(name = "depthRule", description = "深度规则")
@AllArgsConstructor
public class DepthRuleController {

    private final DepthRuleService depthRuleService;

    @Operation(summary = "添加深度规则")
    @PostMapping("/add")
    public ApiResponse add(@Valid @RequestBody DepthRuleDto depthRuleDto) {
        depthRuleService.depthRuleAdd(depthRuleDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "修改深度规则")
    @PostMapping("/update")
    public ApiResponse update(@Valid @RequestBody DepthRuleUpdateDto depthRuleUpdateDto) {
        depthRuleService.depthRuleUpdate(depthRuleUpdateDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "修改深度规则状态")
    @PostMapping("/updateStatus")
    public ApiResponse updateStatus(@Valid @RequestBody DepthRuleStatusDto depthRuleStatusDto) {
        depthRuleService.depthRuleUpdateStatus(depthRuleStatusDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "查询深度规则")
    @PostMapping("/get")
    public ApiResponse<List<DepthRuleVo>> get() {
        return ApiResponse.ok(depthRuleService.selectList());
    }
}
