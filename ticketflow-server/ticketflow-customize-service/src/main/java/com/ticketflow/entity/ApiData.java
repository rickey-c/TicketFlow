package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: API调用记录实体
 * @Author: rickey-c
 * @Date: 2025/1/28 14:17
 */
@Data
@TableName("d_api_data")
public class ApiData extends BaseTableData implements Serializable {

    private Long id;

    private String headVersion;

    private String apiAddress;

    private String apiMethod;

    private String apiBody;

    private String apiParams;

    private String apiUrl;

    private String callDayTime;

    private String callHourTime;

    private String callMinuteTime;

    private String callSecondTime;

    private Integer type;
}
