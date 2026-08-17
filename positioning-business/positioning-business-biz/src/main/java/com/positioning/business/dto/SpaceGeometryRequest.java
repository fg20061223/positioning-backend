package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车位几何更新请求（JSON 入参）
 */
@Data
@Schema(description = "车位几何更新请求（JSON 入参）")
public class SpaceGeometryRequest {

    /** 车位ID */
    @Schema(description = "车位ID")
    private Long id;

    /** 车位轮廓 GeoJSON */
    @Schema(description = "车位轮廓 GeoJSON")
    private String geomGeoJson;

    /** 车位入口点 GeoJSON */
    @Schema(description = "车位入口点 GeoJSON")
    private String entranceGeoJson;
}
