package com.ticketflow.client;


import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.AreaGetDto;
import com.ticketflow.dto.AreaSelectDto;
import com.ticketflow.dto.GetChannelDataByCodeDto;
import com.ticketflow.vo.AreaVo;
import com.ticketflow.vo.GetChannelDataVo;
import com.ticketflow.vo.TokenDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import static com.ticketflow.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;


/**
 * @Description: 基础数据服务 feign
 * @Author: rickey-c
 * @Date: 2025/1/24 15:31
 */
@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"base-data-service",fallback  = BaseDataClientFallback.class)
public interface BaseDataClient {
    /**
     * 根据code查询数据
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping("/channel/data/getByCode")
    ApiResponse<GetChannelDataVo> getByCode(GetChannelDataByCodeDto dto);
    
    /**
     * 查询token数据
     * @return 结果
     * */
    @PostMapping(value = "/get")
    ApiResponse<TokenDataVo> get();
    
    /**
     * 根据id集合查询地区列表
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/area/selectByIdList")
    ApiResponse<List<AreaVo>> selectByIdList(AreaSelectDto dto);
    
    /**
     * 根据id查询地区
     * @param dto 参数
     * @return 结果
     * */
    @PostMapping(value = "/area/getById")
    ApiResponse<AreaVo> getById(AreaGetDto dto);
}
