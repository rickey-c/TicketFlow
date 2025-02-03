package com.ticketflow.controller;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.ParentProgramCategoryDto;
import com.ticketflow.dto.ProgramCategoryAddDto;
import com.ticketflow.dto.ProgramCategoryDto;
import com.ticketflow.service.ProgramCategoryService;
import com.ticketflow.vo.ProgramCategoryVo;
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
 * @Description: 节目分类 控制层
 * @Author: rickey-c
 * @Date: 2025/2/3 21:32
 */
@RestController
@RequestMapping("/program/category")
@Tag(name = "programCategoryController", description = "节目类型")
public class ProgramCategoryController {
    @Autowired
    private ProgramCategoryService programCategoryService;

    @Operation(description = "查询所有节目类型")
    @PostMapping("/select/all")
    public ApiResponse<List<ProgramCategoryVo>> selectAll() {
        return ApiResponse.ok(programCategoryService.selectAll());
    }

    @Operation(description = "通过类型查询节目类型")
    @PostMapping("/select/type")
    public ApiResponse<List<ProgramCategoryVo>> selectByType(@Valid @RequestBody ProgramCategoryDto programCategoryDto) {
        return ApiResponse.ok(programCategoryService.selectByType(programCategoryDto));
    }

    @Operation(summary = "通过父节目类型查询子节目类型")
    @PostMapping("/select/parent")
    public ApiResponse<List<ProgramCategoryVo>> selectByParentProgramCategoryId(@Valid @RequestBody ParentProgramCategoryDto parentProgramCategoryDto) {
        return ApiResponse.ok(programCategoryService.selectByParentProgramCategoryId(parentProgramCategoryDto));
    }

    @Operation(summary = "批量添加节目")
    @PostMapping("/save/batch")
    public ApiResponse<Void> saveBatch(List<ProgramCategoryAddDto> programCategoryAddDtoList) {
        programCategoryService.saveBatch(programCategoryAddDtoList);
        return ApiResponse.ok();
    }


}
