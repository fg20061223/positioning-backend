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
 * 车位表
 */
@Data
@TableName("parking_space")
public class ParkingSpace {

    /** 车位ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;

    /** 所属分区ID */
    private Long zoneId;

    /** 车位编号，如 B3-012 */
    private String spaceNo;

    /** 车位类型: NORMAL=普通 DISABLED=无障碍 CHARGING=充电 COMPACT=微型 MECHANICAL=机械 MOTHER_CHILD=母婴 */
    private String spaceType;

    /** 车位状态: FREE=空闲 OCCUPIED=占用 LOCKED=锁定 FAULT=故障 */
    private String status;

    /** 占用信息来源: APP/CAMERA/MAGNET/GATE/MANUAL */
    private String occupySource;

    /** 车位轮廓（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String geom;

    /** 车位中心点（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String centerPoint;

    /** 车位开口点（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String entrancePoint;

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
