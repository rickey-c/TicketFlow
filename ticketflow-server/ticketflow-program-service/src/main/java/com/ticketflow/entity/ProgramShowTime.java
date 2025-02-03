package com.ticketflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.data.BaseTableData;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 节目演出时间 实体
 * @Author: rickey-c
 * @Date: 2025/2/3 19:45
 */
@Data
@TableName("d_program_show_time")
public class ProgramShowTime extends BaseTableData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 节目表id
     */
    private Long programId;

    /**
     * 演出时间
     */
    private Date showTime;

    /**
     * 演出时间(精确到天)
     */
    private Date showDayTime;

    /**
     * 演出时间所在的星期
     */
    private String showWeekTime;
}
