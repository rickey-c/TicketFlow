package com.ticketflow.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Description: api调用记录dto
 * @Author: rickey-c
 * @Date: 2025/1/29 20:41
 */
@Data
public class ApiDataDto {

    private Long id;

    private String headVersion;

    private String apiAddress;

    private String apiMethod;

    private String apiBody;

    private String apiParams;

    private String apiUrl;

    private Date createTime;

    private Integer status;

    private String callDayTime;

    private String callHourTime;

    private String callMinuteTime;

    private String callSecondTime;

    private Integer type;

}
