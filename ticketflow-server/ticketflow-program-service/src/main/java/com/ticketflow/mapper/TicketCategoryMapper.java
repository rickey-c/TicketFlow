package com.ticketflow.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.dto.TicketCategoryCountDto;
import com.ticketflow.entity.TicketCategory;
import com.ticketflow.entity.TicketCategoryAggregate;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Description: 票档 mapper
 * @Author: rickey-c
 * @Date: 2025/2/3 19:51
 */
public interface TicketCategoryMapper extends BaseMapper<TicketCategory> {

    /**
     * 票档统计
     *
     * @param programIdList 参数
     * @return 结果
     */
    List<TicketCategoryAggregate> selectAggregateList(@Param("programIdList") List<Long> programIdList);

    /**
     * 更新数量
     *
     * @param number 数量
     * @param id     id
     * @return 结果
     */
    int updateRemainNumber(@Param("number") Long number, @Param("id") Long id);

    /**
     * 批量更新数量
     *
     * @param ticketCategoryCountDtoList 参数
     * @param programId                  参数
     * @return 结果
     */
    int batchUpdateRemainNumber(@Param("ticketCategoryCountDtoList")
                                List<TicketCategoryCountDto> ticketCategoryCountDtoList,
                                @Param("programId")
                                Long programId);
}
