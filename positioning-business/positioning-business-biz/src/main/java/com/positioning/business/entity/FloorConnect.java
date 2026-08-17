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
 * 跨楼层连接表: 电梯/扶梯/楼梯的上下层节点绑定
 */
@Schema(description = "跨楼层连接表: 电梯/扶梯/楼梯的上下层节点绑定")
@Data
@TableName("floor_connect")
public class FloorConnect {

    /** 跨层连接ID（雪花ID，应用层生成） */
    @Schema(description = "跨层连接ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 起始楼层ID */
    @Schema(description = "起始楼层ID")
    private Long fromFloorId;

    /** 目标楼层ID */
    @Schema(description = "目标楼层ID")
    private Long toFloorId;

    /** 连接方式: ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯 */
    @Schema(description = "连接方式: ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯")
    private String connectType;

    /** 起始层导航节点ID */
    @Schema(description = "起始层导航节点ID")
    private Long fromNodeId;

    /** 目标层导航节点ID */
    @Schema(description = "目标层导航节点ID")
    private Long toNodeId;

    /** 跨层折算成本（米），电梯等待/绕行按此加权 */
    @Schema(description = "跨层折算成本（米），电梯等待/绕行按此加权")
    private Double costM;

    /** 跨层连接线几何（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @Schema(description = "跨层连接线几何（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口）")
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String geom;

    /** 连接状态: 1=可用 0=不可用 */
    @Schema(description = "连接状态: 1=可用 0=不可用")
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
