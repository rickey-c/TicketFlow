package com.ticketflow.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ticketflow.dto.ProgramListDto;
import com.ticketflow.dto.ProgramPageListDto;
import com.ticketflow.entity.Program;
import com.ticketflow.entity.ProgramJoinShowTime;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Description: 节目 mapper
 * @Author: rickey-c
 * @Date: 2025/2/3 19:51
 */
public interface ProgramMapper extends BaseMapper<Program> {

    /**
     * 主页查询
     *
     * @param programListDto 参数
     * @return 结果
     */
    List<Program> selectHomeList(@Param("programListDto") ProgramListDto programListDto);

    /**
     * 分页查询
     *
     * @param page               分页对象
     * @param programPageListDto 参数
     * @return 结果
     */
    IPage<ProgramJoinShowTime> selectPage(IPage<ProgramJoinShowTime> page,
                                          @Param("programPageListDto") ProgramPageListDto programPageListDto);
}
