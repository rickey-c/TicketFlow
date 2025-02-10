package com.ticketflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.ApiDataDto;
import com.ticketflow.service.ApiDataService;
import com.ticketflow.vo.ApiDataVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: api调用记录 控制层
 * @Author: rickey-c
 * @Date: 2025/1/29 2:01
 */
@RestController
@RequestMapping("/apiData")
@Tag(name = "apiData", description = "api调用记录")
@AllArgsConstructor
public class ApiDataController {

    private final ApiDataService apiDataService;

    @Operation(summary = "分页查询api调用记录")
    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public ApiResponse<Page<ApiDataVo>> pageList(@Valid @RequestBody ApiDataDto dto) {
        return ApiResponse.ok(apiDataService.pageList(dto));
    }
}
