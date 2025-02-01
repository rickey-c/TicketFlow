package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 购票人实体
 * @Author: rickey-c
 * @Date: 2025/1/31 23:17
 */
@Data
@TableName("d_ticket_user")
public class TicketUser extends BaseTableData implements Serializable {

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
     * 用户真实名字
     */
    private String relName;

    /**
     * 证件类型 1:身份证 2:港澳台居民居住证 3:港澳居民来往内地通行证 4:台湾居民来往内地通行证 5:护照 6:外国人永久居住证
     */
    private Integer idType;

    /**
     * 证件号码
     */
    private String idNumber;
}

