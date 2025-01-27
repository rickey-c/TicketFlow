package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: 地区实体
 * @Author: rickey-c
 * @Date: 2025/1/27 21:07
 */
@Data
@TableName("d_area")
public class Area extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 区域id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父区域id
     */
    private Long parentId;
    /**
     * 区域名字
     */
    private String name;

    /**
     * 1:省 2:区 3:县
     */
    private Integer type;

    /**
     * 1:是 0:否
     */
    private Integer municipality;
}
