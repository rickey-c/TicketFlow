package com.ticketflow.service.lua;


import com.ticketflow.vo.SeatVo;
import lombok.Data;

import java.util.List;

/**
 * @Description: 节目缓存更新 实体
 * @Author: rickey-c
 * @Date: 2025/2/7 13:35
 */
@Data
public class ProgramCacheCreateOrderData {

    private Integer code;

    private List<SeatVo> purchaseSeatList;
}
