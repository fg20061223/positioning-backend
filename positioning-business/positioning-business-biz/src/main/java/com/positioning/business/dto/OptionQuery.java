package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 下拉数据查询条件（各字段均为可选过滤条件）
 */
@Data
@Schema(description = "下拉数据查询条件（各字段均为可选过滤条件）")
public class OptionQuery {

    /** 商场ID（可选过滤） */
    @Schema(description = "商场ID（可选过滤）")
    private Long mallId;

    /** 楼层ID（可选过滤） */
    @Schema(description = "楼层ID（可选过滤）")
    private Long floorId;
}
