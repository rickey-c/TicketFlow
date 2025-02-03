package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 座位 实体
 * @Author: rickey-c
 * @Date: 2025/2/3 19:50
 */
@Data
@TableName("d_seat")
public class Seat extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 节目表id
     */
    private Long programId;

    /**
     * 节目票档id
     */
    private Long ticketCategoryId;

    /**
     * 排号
     */
    private Integer rowCode;

    /**
     * 列号
     */
    private Integer colCode;

    /**
     * 座位类型 详见seatType枚举
     */
    private Integer seatType;

    /**
     * 座位价格
     */
    private BigDecimal price;

    /**
     * 1未售卖 2锁定 3已售卖
     */
    private Integer sellStatus;
}
