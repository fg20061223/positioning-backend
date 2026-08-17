package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商铺几何（GeoJSON）
 */
@Data
@Schema(description = "商铺几何（GeoJSON）")
public class ShopGeometryVO {

    /** 商铺ID */
    @Schema(description = "商铺ID")
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
