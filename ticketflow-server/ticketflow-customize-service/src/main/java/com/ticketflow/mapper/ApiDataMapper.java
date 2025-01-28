package com.ticketflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticketflow.dto.ApiDataDto;
import com.ticketflow.entity.ApiData;
import com.ticketflow.vo.ApiDataVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: API调用记录Mappper
 * @Author: rickey-c
 * @Date: 2025/1/28 14:17
 */
@Mapper
public interface ApiDataMapper extends BaseMapper<ApiData> {
    /**
     * 分页查询
     * @param page 分页对象
     * @param apiDataDto 参数
     * @return 分页数据
     * */
    Page<ApiDataVo> pageList(Page<ApiData> page, ApiDataDto apiDataDto);
}
