package com.positioning.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导航节点表: 通道点、岔路口、门、电梯口、车位入口、商铺入口等
 */
@Data
@TableName("nav_node")
public class NavNode {

    /** 节点ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;

    /** 节点类型: WAYPOINT=通道点 JUNCTION=岔路口 DOOR=门 ELEVATOR=电梯口 ESCALATOR=扶梯口 STAIR=楼梯口 SPACE_ENTRY=车位入口 SHOP_ENTRY=商铺入口 POI_ENTRY=设施入口 */
    private String nodeType;

    /** 节点名称 */
    private String name;

    /** 节点坐标（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String geom;

    /** 是否可通行: true=可通行 false=通道封闭 */
    private Boolean isAccessible;

    /** 排序号 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记: 0=正常 1=已删除 */
    @TableLogic
    private Integer deleted;
}
