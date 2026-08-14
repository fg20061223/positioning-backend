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
 * 基础设施/兴趣点表: 电梯、扶梯、楼梯、卫生间、出入口、服务台等
 */
@Data
@TableName("poi")
public class Poi {

    /** 设施ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;

    /** 设施类型: ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯 TOILET=卫生间 ENTRANCE=商场出入口 EXIT=车库出口 SERVICE_DESK=服务台 NURSING_ROOM=母婴室 ATM=取款机 */
    private String poiType;

    /** 设施名称 */
    private String poiName;

    /** 设施轮廓（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String geom;

    /** 设施中心点（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String centerPoint;

    /** 设施入口点（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String entrancePoint;

    /** 设施状态: 1=可用 0=不可用 */
    private Integer status;

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
