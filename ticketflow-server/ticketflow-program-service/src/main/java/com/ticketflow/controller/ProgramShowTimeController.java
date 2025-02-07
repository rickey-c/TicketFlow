package com.ticketflow.controller;

import com.ticketflow.ProgramShowTimeService;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.ProgramShowTimeAddDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 节目分类 控制层
 * @Author: rickey-c
 * @Date: 2025/2/3 23:32
 */
@RestController
@RequestMapping("/program/show/time")
@Tag(name = "program-show-time", description = "节目演出时间")
public class ProgramShowTimeController {

    @Autowired
    private ProgramShowTimeService programShowTimeService;

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    public ApiResponse<Long> add(@Valid @RequestBody ProgramShowTimeAddDto programShowTimeAddDto) {
        return ApiResponse.ok(programShowTimeService.add(programShowTimeAddDto));
    }
}
