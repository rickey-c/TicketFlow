package com.ticketflow.controller;

import com.ticketflow.dto.*;
import com.ticketflow.page.PageVo;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.service.ProgramService;
import com.ticketflow.vo.ProgramHomeVo;
import com.ticketflow.vo.ProgramListVo;
import com.ticketflow.vo.ProgramVo;
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
 * @Description: 节目 控制层
 * @Author: rickey-c
 * @Date: 2025/2/8 23:50
 */
@RestController
@RequestMapping("/program")
@Tag(name = "program", description = "节目")
public class ProgramController {

    @Autowired
    private ProgramService programService;

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    public ApiResponse<Long> add(@Valid @RequestBody ProgramAddDto programAddDto) {
        return ApiResponse.ok(programService.add(programAddDto));
    }

    @Operation(summary = "搜索")
    @PostMapping(value = "/search")
    public ApiResponse<PageVo<ProgramListVo>> search(@Valid @RequestBody ProgramSearchDto programSearchDto) {
        return ApiResponse.ok(programService.search(programSearchDto));
    }

    @Operation(summary = "查询主页列表")
    @PostMapping(value = "/home/list")
    public ApiResponse<List<ProgramHomeVo>> selectHomeList(@Valid @RequestBody ProgramListDto programListDto) {
        return ApiResponse.ok(programService.selectHomeList(programListDto));
    }

    @Operation(summary = "查询分页列表")
    @PostMapping(value = "/page")
    public ApiResponse<PageVo<ProgramListVo>> selectPage(@Valid @RequestBody ProgramPageListDto programPageListDto) {
        return ApiResponse.ok(programService.selectPage(programPageListDto));
    }

    @Operation(summary = "查询推荐列表")
    @PostMapping(value = "/recommend/list")
    public ApiResponse<List<ProgramListVo>> recommendList(@Valid @RequestBody ProgramRecommendListDto programRecommendListDto) {
        return ApiResponse.ok(programService.recommendList(programRecommendListDto));
    }

    @Operation(summary = "查询详情(根据id)")
    @PostMapping(value = "/detail")
    public ApiResponse<ProgramVo> getDetail(@Valid @RequestBody ProgramGetDto programGetDto) {
        return ApiResponse.ok(programService.detail(programGetDto));
    }

    @Operation(summary = "查询详情V1(根据id)")
    @PostMapping(value = "/detail/v1")
    public ApiResponse<ProgramVo> getDetailV1(@Valid @RequestBody ProgramGetDto programGetDto) {
        return ApiResponse.ok(programService.detailV1(programGetDto));
    }

    @Operation(summary = "查询详情V2(根据id)")
    @PostMapping(value = "/detail/v2")
    public ApiResponse<ProgramVo> getDetailV2(@Valid @RequestBody ProgramGetDto programGetDto) {
        return ApiResponse.ok(programService.detailV2(programGetDto));
    }

    @Operation(summary = "节目失效(根据id)")
    @PostMapping(value = "/invalid")
    public ApiResponse<Boolean> invalid(@Valid @RequestBody ProgramInvalidDto programInvalidDto) {
        return ApiResponse.ok(programService.invalid(programInvalidDto));
    }

    @Operation(summary = "查看节目详情本地缓存(根据id)")
    @PostMapping(value = "/local/detail")
    public ApiResponse<ProgramVo> localDetail(@Valid @RequestBody ProgramGetDto programGetDto) {
        return ApiResponse.ok(programService.localDetail(programGetDto));
    }
}
