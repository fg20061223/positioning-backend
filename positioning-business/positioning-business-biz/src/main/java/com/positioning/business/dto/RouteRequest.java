package com.positioning.business.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 路径规划请求
 */
@Data
public class RouteRequest {

    /** 所属商场ID */
    @NotNull(message = "商场ID不能为空")
    private Long mallId;

    /** 起点导航节点ID */
    @NotNull(message = "起点节点不能为空")
    private Long fromNodeId;

    /** 终点导航节点ID */
    @NotNull(message = "终点节点不能为空")
    private Long toNodeId;
}
