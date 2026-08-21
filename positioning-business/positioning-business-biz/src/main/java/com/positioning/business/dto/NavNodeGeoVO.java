package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导航节点几何 VO（含 GeoJSON 坐标，供导航图编辑器加载）
 */
@Data
@Schema(description = "导航节点几何 VO（含 GeoJSON 坐标）")
public class NavNodeGeoVO {

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "所属商场ID")
    private Long mallId;

    @Schema(description = "所属楼层ID")
    private Long floorId;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "节点名称")
    private String name;

    /** 节点坐标 GeoJSON（ST_AsGeoJSON 输出） */
    @Schema(description = "节点坐标 GeoJSON")
    private String geomGeoJson;

    @Schema(description = "是否可通行")
    private Boolean isAccessible;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
