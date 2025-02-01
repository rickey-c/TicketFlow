package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 用户手机号实体
 * @Author: rickey-c
 * @Date: 2025/1/31 23:18
 */
@Data
@TableName("d_user_mobile")
public class UserMobile extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 手机号
     */
    private String mobile;
}

