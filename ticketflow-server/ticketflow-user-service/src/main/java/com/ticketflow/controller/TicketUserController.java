package com.ticketflow.controller;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.TicketUserDto;
import com.ticketflow.dto.TicketUserIdDto;
import com.ticketflow.dto.TicketUserListDto;
import com.ticketflow.service.TicketUserService;
import com.ticketflow.vo.TicketUserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 购票人 控制层
 * @Author: rickey-c
 * @Date: 2025/1/31 23:22
 */
@RestController
@RequestMapping("/ticket/user")
@Tag(name = "ticket-user", description = "购票人")
public class TicketUserController {

    @Autowired
    private TicketUserService ticketUserService;

    @Operation(summary = "查询购票人列表")
    @PostMapping(value = "/list")
    public ApiResponse<List<TicketUserVo>> list(@Valid @RequestBody TicketUserListDto ticketUserListDto) {
        return ApiResponse.ok(ticketUserService.list(ticketUserListDto));
    }

    @Operation(summary = "添加购票人")
    @PostMapping(value = "/add")
    public ApiResponse<Void> add(@Valid @RequestBody TicketUserDto ticketUserDto) {
        ticketUserService.add(ticketUserDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "删除购票人")
    @PostMapping(value = "/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody TicketUserIdDto ticketUserIdDto) {
        ticketUserService.delete(ticketUserIdDto);
        return ApiResponse.ok();
    }
}
