package com.ticketflow.controller;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.AllRuleDto;
import com.ticketflow.service.AllRuleService;
import com.ticketflow.vo.AllDepthRuleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 所有规则控制层
 * @Author: rickey-c
 * @Date: 2025/1/28 14:50
 */
@RestController
@RequestMapping("/allRule")
@Tag(name = "allRule", description = "所有规则")
@AllArgsConstructor
public class AllRuleController {

    private final AllRuleService allRuleService;

    @Operation(summary = "添加所有规则")
    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody AllRuleDto allRuleDto) {
        allRuleService.add(allRuleDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "获取所有规则")
    @PostMapping("/get")
    public ApiResponse<AllDepthRuleVo> get() {
        return ApiResponse.ok(allRuleService.get());
    }


}
