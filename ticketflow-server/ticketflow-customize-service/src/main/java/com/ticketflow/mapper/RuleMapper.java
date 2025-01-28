package com.ticketflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.entity.Rule;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: 普通规则Mapper
 * @Author: rickey-c
 * @Date: 2025/1/28 14:47
 */
@Mapper
public interface RuleMapper extends BaseMapper<Rule> {
    /**
     * 删除所有规则
     * @return 结果
     * */
    int delAll();
}
