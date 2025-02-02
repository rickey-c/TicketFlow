package com.ticketflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 节目演出时间 vo
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "ProgramShowTimeVo", description = "节目演出时间")
public class ProgramShowTimeVo {


    /**
     * 主键id
     */
    @Schema(name = "id", type = "Long", description = "主键id")
    private Long id;

    /**
     * 节目表id
     */
    @Schema(name = "programId", type = "Long", description = "节目表id")
    private Long programId;

    /**
     * 演出时间
     */
    @Schema(name = "showTime", type = "Date", description = "演出时间")
    private Date showTime;

    /**
     * 演出时间(精确到天)
     */
    @Schema(name = "showDayTime", type = "Date", description = "演出时间(精确到天)")
    private Date showDayTime;

    /**
     * 演出时间所在的星期
     */
    @Schema(name = "showWeekTime", type = "String", description = "演出时间所在的星期")
    private String showWeekTime;
}
