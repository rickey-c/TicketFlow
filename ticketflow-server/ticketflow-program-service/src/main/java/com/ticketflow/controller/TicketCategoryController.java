package com.ticketflow.controller;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.TicketCategoryAddDto;
import com.ticketflow.dto.TicketCategoryDto;
import com.ticketflow.service.TicketCategoryService;
import com.ticketflow.vo.TicketCategoryDetailVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 节目票档 控制层
 * @Author: rickey-c
 * @Date: 2025/2/3 23:32
 */
@RestController
@RequestMapping("/ticket/category")
@Tag(name = "ticket-category", description = "票档")
public class TicketCategoryController {

    @Autowired
    private TicketCategoryService ticketCategoryService;

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    public ApiResponse<Long> add(@Valid @RequestBody TicketCategoryAddDto ticketCategoryAddDto) {
        return ApiResponse.ok(ticketCategoryService.add(ticketCategoryAddDto));
    }

    @Operation(summary = "查询详情")
    @PostMapping(value = "/detail")
    public ApiResponse<TicketCategoryDetailVo> detail(@Valid @RequestBody TicketCategoryDto ticketCategoryDto) {
        return ApiResponse.ok(ticketCategoryService.detail(ticketCategoryDto));
    }
}
