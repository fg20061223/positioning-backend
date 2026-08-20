package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 楼层列表查询请求（by-mall 扩展条件）
 * <p>mallId 与 id 均可指定商场（id 兼容旧的 {"id":商场ID} 入参）</p>
 */
@Data
@Schema(description = "楼层列表查询请求（by-mall, 可按楼层编码/名称/排序号过滤）")
public class FloorByMallQuery {

    /** 商场ID（推荐） */
    @Schema(description = "商场ID（推荐）")
    private Long mallId;

    /** 商场ID（兼容旧入参 {"id":1}） */
    @Schema(description = "商场ID（兼容旧入参 {\"id\":1}）")
    private Long id;

    /** 楼层编码（模糊, 如 B2/B1/1F） */
    @Schema(description = "楼层编码（模糊, 如 B2/B1/1F）")
    private String floorCode;

    /** 楼层名称（模糊） */
    @Schema(description = "楼层名称（模糊）")
    private String floorName;

    /** 排序号（精确匹配） */
    @Schema(description = "排序号（精确匹配）")
    private Integer sortOrder;
}
