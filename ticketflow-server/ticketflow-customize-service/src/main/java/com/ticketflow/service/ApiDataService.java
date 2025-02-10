package com.ticketflow.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticketflow.core.RepeatExecuteLimitConstants;
import com.ticketflow.dto.ApiDataDto;
import com.ticketflow.entity.ApiData;
import com.ticketflow.mapper.ApiDataMapper;
import com.ticketflow.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.ApiDataVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: api调用记录service
 * @Author: rickey-c
 * @Date: 2025/1/29 1:43
 */
@Slf4j
@Service
@AllArgsConstructor
public class ApiDataService {

    private final ApiDataMapper apiDataMapper;

    @RepeatExecuteLimit(name = RepeatExecuteLimitConstants.CONSUMER_API_DATA_MESSAGE, keys = {"#apiData.id"})
    public void saveApiData(ApiData apiData) {
        ApiData dbApiData = apiDataMapper.selectById(apiData.getId());
        if (Objects.isNull(dbApiData)) {
            log.info("saveApiData apiData:{}", JSON.toJSONString(apiData));
            apiDataMapper.insert(apiData);
        }
    }

    public Page<ApiDataVo> pageList(ApiDataDto dto) {
        // 构造Page参数，从数据库从获取 Page
        Page<ApiData> page = Page.of(dto.getPageNo(), dto.getPageSize());
        LambdaQueryWrapper<ApiData> queryWrapper = Wrappers.lambdaQuery(ApiData.class)
                .eq(StringUtil.isNotEmpty(dto.getApiAddress()), ApiData::getApiAddress, dto.getApiAddress())
                .eq(StringUtil.isNotEmpty(dto.getApiUrl()), ApiData::getApiUrl, dto.getApiUrl())
                .ge(Objects.nonNull(dto.getStartDate()), ApiData::getCreateTime, dto.getStartDate())
                .le(Objects.nonNull(dto.getEndDate()), ApiData::getCreateTime, dto.getEndDate());
        Page<ApiData> apiDataPage = apiDataMapper.selectPage(page, queryWrapper);
        // 构造 VoPage
        Page<ApiDataVo> apiDataPageVo = new Page<>();
        BeanUtils.copyProperties(apiDataPage, apiDataPageVo);

        // 构造 VoPage的记录
        List<ApiData> apiDataList = apiDataPage.getRecords();
        List<ApiDataVo> apiDataVoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(apiDataList)) {
            apiDataVoList = apiDataList.stream().map(
                    apiData -> {
                        ApiDataVo apiDataVo = new ApiDataVo();
                        BeanUtils.copyProperties(apiData, apiDataVo);
                        return apiDataVo;
                    }
            ).collect(Collectors.toList());
        }
        // 设置记录，并返回
        apiDataPageVo.setRecords(apiDataVoList);
        return apiDataPageVo;
    }
}
