package com.positioning.business.dto;

import com.positioning.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商铺条件分页查询请求（JSON 入参）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopQuery extends PageQuery {

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;

    /** 商铺分类ID */
    private Long categoryId;

    /** 商铺状态: OPEN/DECORATING/CLOSED */
    private String status;

    /** 搜索关键词（名称/关键词模糊匹配） */
    private String keyword;
}
