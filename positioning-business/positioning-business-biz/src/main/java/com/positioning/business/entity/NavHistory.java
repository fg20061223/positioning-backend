package com.positioning.business.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导航历史表: 记录用户每次导航的起终点、路径与耗时
 */
@Data
@TableName("nav_history")
public class NavHistory {

    /** 导航记录ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID（可为空） */
    private Long userId;

    /** 所属商场ID */
    private Long mallId;

    /** 起始楼层ID */
    private Long fromFloorId;

    /** 目标楼层ID */
    private Long toFloorId;

    /** 起点类型: CURRENT_POSITION=当前位置 SPACE=车位 SHOP=商铺 POI=设施 */
    private String fromType;

    /** 起点目标ID */
    private Long fromTargetId;

    /** 终点类型: SPACE=车位 SHOP=商铺 POI=设施 */
    private String toType;

    /** 终点目标ID */
    private Long toTargetId;

    /** 整条路径线（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String pathGeom;

    /** 路径总距离（米） */
    private Double distanceM;

    /** 导航耗时（秒） */
    private Integer durationS;

    /** 导航状态: FINISHED=完成 ABORTED=中途放弃 */
    private String status;

    /** 导航开始时间 */
    private LocalDateTime startTime;

    /** 导航结束时间 */
    private LocalDateTime endTime;

    /** 创建时间 */
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 逻辑删除标记: 0=正常 1=已删除 */
    @TableLogic
    private Integer deleted;
}
