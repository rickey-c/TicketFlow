package com.ticketflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.entity.DepthRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: 深度规则Mapper
 * @Author: rickey-c
 * @Date: 2025/1/28 14:47
 */
@Mapper
public interface DepthRuleMapper extends BaseMapper<DepthRule> {

    /**
     * 删除所有规则
     * @return 结果
     * */
    int delAll();
}
