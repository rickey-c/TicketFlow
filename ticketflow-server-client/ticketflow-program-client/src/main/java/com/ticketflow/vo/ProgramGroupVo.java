package com.ticketflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: 节目分组 vo
 * @Author: rickey-c
 * @Date: 2025/2/2 21:25
 */
@Data
@Schema(title = "ProgramGroupVo", description = "节目分组")
public class ProgramGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", type = "Long", description = "主键id")
    private Long id;

    @Schema(name = "programSimpleInfoVoList", type = "List<ProgramSimpleInfoVo>", description = "节目简单信息集合")
    private List<ProgramSimpleInfoVo> programSimpleInfoVoList;

    @Schema(name = "recentShowTime", type = "Date", description = "最近的节目演出时间")
    private Date recentShowTime;
}
