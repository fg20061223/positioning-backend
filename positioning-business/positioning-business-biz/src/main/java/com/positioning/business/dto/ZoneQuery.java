package com.positioning.business.dto;

import com.positioning.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分区条件分页查询请求（JSON 入参）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分区条件分页查询请求（JSON 入参）")
public class ZoneQuery extends PageQuery {

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 所属楼层ID */
    @Schema(description = "所属楼层ID")
    private Long floorId;

    /** 分区编码（模糊, 如 A区/B区） */
    @Schema(description = "分区编码（模糊, 如 A区/B区）")
    private String zoneCode;

    /** 分区名称（模糊） */
    @Schema(description = "分区名称（模糊）")
    private String zoneName;
}
