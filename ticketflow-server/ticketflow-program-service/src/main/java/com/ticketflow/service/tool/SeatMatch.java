package com.ticketflow.service.tool;

import com.ticketflow.vo.SeatVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description: 自动选座 座位匹配算法
 * @Author: rickey-c
 * @Date: 2025/2/3 20:34
 */
public class SeatMatch {

    public static List<SeatVo> findAdjacentSeatVos(List<SeatVo> allSeats, int seatCount) {
        List<SeatVo> adjacentSeats = new ArrayList<>();

        // 按照行号，列号排序
        allSeats.sort((s1, s2) -> {
            if (Objects.equals(s1.getRowCode(), s2.getRowCode())) {
                return s1.getColCode() - s2.getColCode();
            } else {
                return s1.getRowCode() - s2.getRowCode();
            }
        });

        for (int i = 0; i < allSeats.size() - seatCount + 1; i++) {
            boolean seatsFound = true;
            for (int j = 0; j < seatCount - 1; j++) {
                SeatVo current = allSeats.get(i + j);
                SeatVo next = allSeats.get(i + j + 1);
                // 行号相同，且列号相差和为1，我们认为是相邻座位，这里是否定条件
                if (!Objects.equals(current.getRowCode(), next.getRowCode())
                        || next.getColCode() - current.getColCode() != 1) {
                    seatsFound = false;
                    break;
                }
            }
            if (seatsFound) {
                for (int k = 0; k < seatCount; k++) {
                    adjacentSeats.add(allSeats.get(i + k));
                }
                return adjacentSeats;
            }
        }
        return adjacentSeats;

    }
}
