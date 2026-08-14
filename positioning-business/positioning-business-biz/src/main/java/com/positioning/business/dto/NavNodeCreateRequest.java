package com.positioning.business.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 导航节点创建请求（含几何 GeoJSON）
 */
@Data
public class NavNodeCreateRequest {

    /** 所属商场ID */
    @NotNull(message = "商场ID不能为空")
    private Long mallId;

    /** 所属楼层ID */
    @NotNull(message = "楼层ID不能为空")
    private Long floorId;

    /** 节点类型 */
    private String nodeType;

    /** 节点名称 */
    private String name;

    /** 节点坐标 GeoJSON（如 {"type":"Point","coordinates":[x,y]}） */
    private String geomGeoJson;

    /** 是否可通行 */
    private Boolean isAccessible;

    /** 排序号 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;
}
