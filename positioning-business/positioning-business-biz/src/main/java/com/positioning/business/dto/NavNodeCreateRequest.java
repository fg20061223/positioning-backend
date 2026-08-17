package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 导航节点创建请求（含几何 GeoJSON）
 */
@Data
@Schema(description = "导航节点创建请求（含几何 GeoJSON）")
public class NavNodeCreateRequest {

    /** 所属商场ID */
    @NotNull(message = "商场ID不能为空")
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 所属楼层ID */
    @NotNull(message = "楼层ID不能为空")
    @Schema(description = "所属楼层ID")
    private Long floorId;

    /** 节点类型 */
    @Schema(description = "节点类型")
    private String nodeType;

    /** 节点名称 */
    @Schema(description = "节点名称")
    private String name;

    /** 节点坐标 GeoJSON（如 {"type":"Point","coordinates":[x,y]}） */
    @Schema(description = "节点坐标 GeoJSON（如 {\"type\":\"Point\",\"coordinates\":[x,y]}）")
    private String geomGeoJson;

    /** 是否可通行 */
    @Schema(description = "是否可通行")
    private Boolean isAccessible;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
