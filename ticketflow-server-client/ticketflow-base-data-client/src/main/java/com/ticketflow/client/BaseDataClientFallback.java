package com.ticketflow.client;


import com.ticketflow.common.ApiResponse;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.dto.AreaGetDto;
import com.ticketflow.dto.AreaSelectDto;
import com.ticketflow.dto.GetChannelDataByCodeDto;
import com.ticketflow.vo.AreaVo;
import com.ticketflow.vo.GetChannelDataVo;
import com.ticketflow.vo.TokenDataVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 用户服务 feign 异常处理
 * @Author: rickey-c
 * @Date: 2025/1/24 15:37
 */
@Component
public class BaseDataClientFallback implements BaseDataClient{
    @Override
    public ApiResponse<GetChannelDataVo> getByCode(final GetChannelDataByCodeDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<TokenDataVo> get() {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<List<AreaVo>> selectByIdList(final AreaSelectDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
    
    @Override
    public ApiResponse<AreaVo> getById(final AreaGetDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
