package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导航节点几何更新请求
 * <p>
 * 说明: 导航节点 geom 为 NOT NULL 列且通用 CRUD 不读写,
 * 编辑器拖拽移动节点时必须走 POST /business/nav-node/update-geometry。
 */
@Data
@Schema(description = "导航节点几何更新请求（编辑器拖拽移动节点）")
public class NavNodeGeometryRequest {

    /** 节点ID */
    @Schema(description = "节点ID")
    private Long id;

    /** 节点坐标（GeoJSON Point, 楼层本地米制坐标, SRID=0） */
    @Schema(description = "节点坐标（GeoJSON Point, 楼层本地米制坐标, SRID=0）")
    private String geomGeoJson;
}
