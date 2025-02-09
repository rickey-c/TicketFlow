package com.ticketflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.entity.OrderTicketUser;
import com.ticketflow.entity.OrderTicketUserAggregate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 购票人订单 mapper
 * @Author: rickey-c
 * @Date: 2025/2/9 17:18
 */
public interface OrderTicketUserMapper extends BaseMapper<OrderTicketUser> {

    /**
     * 查询订单下购票人数量
     *
     * @param orderNumberList 参数
     * @return 结果
     */
    List<OrderTicketUserAggregate> selectOrderTicketUserAggregate(@Param("orderNumberList") List<Long> orderNumberList);

}
