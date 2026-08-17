package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商铺几何更新请求（JSON 入参）
 */
@Data
@Schema(description = "商铺几何更新请求（JSON 入参）")
public class ShopGeometryRequest {

    /** 商铺ID */
    @Schema(description = "商铺ID")
    private Long id;

    /** 商铺轮廓 GeoJSON */
    @Schema(description = "商铺轮廓 GeoJSON")
    private String geomGeoJson;

    /** 商铺入口点 GeoJSON */
    @Schema(description = "商铺入口点 GeoJSON")
    private String entranceGeoJson;
}
