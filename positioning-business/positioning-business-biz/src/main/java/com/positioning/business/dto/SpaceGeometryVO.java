package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车位几何（GeoJSON）
 */
@Data
@Schema(description = "车位几何（GeoJSON）")
public class SpaceGeometryVO {

    /** 车位ID */
    @Schema(description = "车位ID")
    private Long id;

    /** 轮廓 GeoJSON */
    @Schema(description = "轮廓 GeoJSON")
    private String geomGeojson;

    /** 中心点 GeoJSON */
    @Schema(description = "中心点 GeoJSON")
    private String centerGeojson;

    /** 入口点 GeoJSON */
    @Schema(description = "入口点 GeoJSON")
    private String entranceGeojson;
}
