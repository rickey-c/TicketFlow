package com.ticketflow.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Description: 用户vo
 * @Author: rickey-c
 * @Date: 2025/1/29 21:36
 */
@Data
public class UserVo {

    private String id;

    private String name;

    private String password;

    private Integer age;

    private Integer status;

    private Date createTime;

    private String mobile;

    private Date editTime;
}
