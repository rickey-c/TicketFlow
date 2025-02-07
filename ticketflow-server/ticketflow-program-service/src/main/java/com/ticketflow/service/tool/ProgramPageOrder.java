package com.ticketflow.service.tool;

import lombok.Data;
import org.elasticsearch.search.sort.SortOrder;


/**
 * @Description: 节目排序实体
 * @Author: rickey-c
 * @Date: 2025/2/3 20:31
 */
@Data
public class ProgramPageOrder {

    public String sortParam;

    public SortOrder sortOrder;

}
