package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 普通规则实体
 * @Author: rickey-c
 * @Date: 2025/1/28 14:17
 */
@Data
@TableName("d_rule")
public class Rule extends BaseTableData implements Serializable {
    
    private Long id;

    private Integer statTime;
    
    private Integer statTimeType;
    
    private Integer threshold;
    
    private Integer effectiveTime;
    
    private Integer effectiveTimeType;
    
    private String limitApi;
    
    private String message;
}
