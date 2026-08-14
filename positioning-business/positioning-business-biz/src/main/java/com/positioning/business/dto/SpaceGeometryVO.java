package com.positioning.business.dto;

import lombok.Data;

/**
 * 车位几何（GeoJSON）
 */
@Data
public class SpaceGeometryVO {

    /** 车位ID */
    private Long id;

    /** 轮廓 GeoJSON */
    private String geomGeojson;

    /** 中心点 GeoJSON */
    private String centerGeojson;

    /** 入口点 GeoJSON */
    private String entranceGeojson;
}
