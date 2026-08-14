package com.positioning.business.dto;

import lombok.Data;

/**
 * 商铺模糊搜索请求（JSON 入参）
 */
@Data
public class ShopSearchRequest {

    /** 所属商场ID */
    private Long mallId;

    /** 搜索关键词（如 海底捞） */
    private String keyword;

    /** 返回条数上限（默认20） */
    private Integer limit;
}
