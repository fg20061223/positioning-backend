package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 路径规划请求
 */
@Data
@Schema(description = "路径规划请求")
public class RouteRequest {

    /** 所属商场ID */
    @NotNull(message = "商场ID不能为空")
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 起点导航节点ID */
    @NotNull(message = "起点节点不能为空")
    @Schema(description = "起点导航节点ID")
    private Long fromNodeId;

    /** 终点导航节点ID */
    @NotNull(message = "终点节点不能为空")
    @Schema(description = "终点导航节点ID")
    private Long toNodeId;
}
