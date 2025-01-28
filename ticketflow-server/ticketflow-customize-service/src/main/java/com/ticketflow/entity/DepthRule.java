package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 深度规则实体
 * @Author: rickey-c
 * @Date: 2025/1/28 14:17
 */
@Data
@TableName("d_depth_rule")
public class DepthRule extends BaseTableData implements Serializable {
    
    private Long id;
    
    private String startTimeWindow;
    
    private String endTimeWindow;

    private Integer statTime;
    
    private Integer statTimeType;
    
    private Integer threshold;
    
    private Integer effectiveTime;
    
    private Integer effectiveTimeType;
    
    private String limitApi;
    
    private String message;
}
