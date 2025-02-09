package com.ticketflow.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.entity.Order;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 订单 mapper
 * @Author: rickey-c
 * @Date: 2025/2/9 17:18
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询账户下购票人数量
     *
     * @param userId    用户id
     * @param programId 节目id
     * @return 结果
     */
    Integer accountOrderCount(@Param("userId") Long userId, @Param("programId") Long programId);
}
