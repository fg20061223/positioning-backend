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
 * 蓝牙信标表: 室内定位的物理设备点位
 */
@Schema(description = "蓝牙信标表: 室内定位的物理设备点位")
@Data
@TableName("beacon")
public class Beacon {

    /** 信标ID（雪花ID，应用层生成） */
    @Schema(description = "信标ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 所属楼层ID */
    @Schema(description = "所属楼层ID")
    private Long floorId;

    /** iBeacon广播UUID */
    @Schema(description = "iBeacon广播UUID")
    private String uuid;

    /** iBeacon major编号 */
    @Schema(description = "iBeacon major编号")
    private Integer major;

    /** iBeacon minor编号 */
    @Schema(description = "iBeacon minor编号")
    private Integer minor;

    /** 信标MAC地址 */
    @Schema(description = "信标MAC地址")
    private String mac;

    /** 信标协议类型: IBEACON/EDDYSTONE */
    @Schema(description = "信标协议类型: IBEACON/EDDYSTONE")
    private String beaconType;

    /** 信标布点坐标（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @Schema(description = "信标布点坐标（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口）")
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String positionGeom;

    /** 1米处RSSI参考值（定位校准用） */
    @Schema(description = "1米处RSSI参考值（定位校准用）")
    private Integer txPower;

    /** 信标电量百分比 */
    @Schema(description = "信标电量百分比")
    private Integer battery;

    /** 信标状态: ACTIVE=正常 INACTIVE=停用 FAULT=故障 */
    @Schema(description = "信标状态: ACTIVE=正常 INACTIVE=停用 FAULT=故障")
    private String status;

    /** 最近一次心跳上报时间 */
    @Schema(description = "最近一次心跳上报时间")
    private LocalDateTime lastReportAt;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

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
