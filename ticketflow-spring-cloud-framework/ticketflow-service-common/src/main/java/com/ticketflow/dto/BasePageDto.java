package com.ticketflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: 分页基础参数
 * @Author: rickey-c
 * @Date: 2025/1/24 16:55
 */
@Data
public class BasePageDto {
    @NotNull
    private Integer pageNumber;


    @NotNull
    private Integer pageSize;
}
