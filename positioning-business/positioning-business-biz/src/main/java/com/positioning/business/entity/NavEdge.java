package com.positioning.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导航边表: 连通导航节点的通行线段
 */
@Schema(description = "导航边表: 连通导航节点的通行线段")
@Data
@TableName("nav_edge")
public class NavEdge {

    /** 边ID（雪花ID，应用层生成） */
    @Schema(description = "边ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 起始节点ID */
    @Schema(description = "起始节点ID")
    private Long fromNodeId;

    /** 终点节点ID */
    @Schema(description = "终点节点ID")
    private Long toNodeId;

    /** 边类型: WALK=步行 ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯 */
    @Schema(description = "边类型: WALK=步行 ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯")
    private String edgeType;

    /** 边长（米），路径规划直接使用 */
    @Schema(description = "边长（米），路径规划直接使用")
    private Double distanceM;

    /** 通行成本系数（上下楼/绕行加权，默认1） */
    @Schema(description = "通行成本系数（上下楼/绕行加权，默认1）")
    private Double weight;

    /** 边几何（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @Schema(description = "边几何（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口）")
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String geom;

    /** 是否双向通行: true=双向 false=单向 */
    @Schema(description = "是否双向通行: true=双向 false=单向")
    private Boolean bidirectional;

    /** 边状态: 1=可用 0=不可用 */
    @Schema(description = "边状态: 1=可用 0=不可用")
    private Integer status;

    /** 创建时间 */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记: 0=正常 1=已删除 */
    @Schema(description = "逻辑删除标记: 0=正常 1=已删除")
    @TableLogic
    private Integer deleted;
}
