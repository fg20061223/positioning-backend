package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车位号搜索请求（JSON 入参）
 */
@Data
@Schema(description = "车位号搜索请求（JSON 入参）")
public class SpaceSearchRequest {

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 搜索关键词（车位号片段） */
    @Schema(description = "搜索关键词（车位号片段）")
    private String keyword;

    /** 返回条数上限（默认20） */
    @Schema(description = "返回条数上限（默认20）")
    private Integer limit;
}
