package com.positioning.business.dto;

import com.positioning.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 信标条件分页查询请求（JSON 入参）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "信标条件分页查询请求（JSON 入参）")
public class BeaconQuery extends PageQuery {

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 所属楼层ID */
    @Schema(description = "所属楼层ID")
    private Long floorId;

    /** 信标状态: ACTIVE/INACTIVE/FAULT */
    @Schema(description = "信标状态: ACTIVE/INACTIVE/FAULT")
    private String status;
}
