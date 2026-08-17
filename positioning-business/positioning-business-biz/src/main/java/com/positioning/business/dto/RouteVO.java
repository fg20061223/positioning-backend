package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 路径规划结果
 */
@Data
@Schema(description = "路径规划结果")
public class RouteVO {

    /** 有序节点ID列表（含起终点） */
    @Schema(description = "有序节点ID列表（含起终点）")
    private List<Long> nodeIds;

    /** 有序路径段列表 */
    @Schema(description = "有序路径段列表")
    private List<RouteSegment> segments;

    /** 路径总距离（米, 含跨层折算成本） */
    @Schema(description = "路径总距离（米, 含跨层折算成本）")
    private Double totalDistanceM;

    /**
     * 路径段
     */
    @Data
    @Schema(description = "路径段")
    public static class RouteSegment {

        /** 起始节点ID */
        @Schema(description = "起始节点ID")
        private Long fromNodeId;

        /** 终点节点ID */
        @Schema(description = "终点节点ID")
        private Long toNodeId;

        /** 边ID（跨层时为 floor_connect ID） */
        @Schema(description = "边ID（跨层时为 floor_connect ID）")
        private Long edgeId;

        /** 通行类型: WALK/ELEVATOR/ESCALATOR/STAIR */
        @Schema(description = "通行类型: WALK/ELEVATOR/ESCALATOR/STAIR")
        private String edgeType;

        /** 是否跨楼层 */
        @Schema(description = "是否跨楼层")
        private Boolean crossFloor;

        /** 本段距离/成本（米） */
        @Schema(description = "本段距离/成本（米）")
        private Double distanceM;
    }
}
