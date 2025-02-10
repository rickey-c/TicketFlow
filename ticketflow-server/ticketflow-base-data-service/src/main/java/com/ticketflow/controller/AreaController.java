package com.ticketflow.controller;


import com.ticketflow.dto.AreaGetDto;
import com.ticketflow.dto.AreaSelectDto;
import com.ticketflow.service.AreaService;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.vo.AreaVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 地区Controller
 * @Author: rickey-c
 * @Date: 2025/1/27 21:05
 */
@RestController
@RequestMapping("/area")
@Tag(name = "area", description = "区域")
public class AreaController {

    @Autowired
    private AreaService areaService;

    @Operation(summary = "查询市区以及直辖市数据")
    @PostMapping(value = "/selectCityData")
    public ApiResponse<List<AreaVo>> selectCityData() {
        return ApiResponse.ok(areaService.selectCityData());
    }

    @Operation(summary = "查询数据根据id集合")
    @PostMapping(value = "/selectByIdList")
    public ApiResponse<List<AreaVo>> selectByIdList(@Valid @RequestBody AreaSelectDto areaSelectDto) {
        return ApiResponse.ok(areaService.selectByIdList(areaSelectDto));
    }

    @Operation(summary = "查询数据根据id")
    @PostMapping(value = "/getById")
    public ApiResponse<AreaVo> getById(@Valid @RequestBody AreaGetDto areaGetDto) {
        return ApiResponse.ok(areaService.getById(areaGetDto));
    }

    @Operation(summary = "当前城市")
    @PostMapping(value = "/current")
    public ApiResponse<AreaVo> current() {
        // 这里没有实现定位，默认是北京
        return ApiResponse.ok(areaService.current());
    }

    @Operation(summary = "热门城市")
    @PostMapping(value = "/hot")
    public ApiResponse<List<AreaVo>> hot() {
        return ApiResponse.ok(areaService.hot());
    }
}
