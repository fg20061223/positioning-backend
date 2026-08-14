package com.positioning.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页查询请求（JSON 入参）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {

    /** 页码（从1开始，默认1） */
    private long pageNum = 1;

    /** 每页条数（默认10） */
    private long pageSize = 10;
}
