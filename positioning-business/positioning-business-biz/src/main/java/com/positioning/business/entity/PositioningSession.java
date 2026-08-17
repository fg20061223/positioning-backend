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
 * 定位会话表: 记录用户一次进入商场的定位过程
 */
@Schema(description = "定位会话表: 记录用户一次进入商场的定位过程")
@Data
@TableName("positioning_session")
public class PositioningSession {

    /** 会话ID（雪花ID，应用层生成） */
    @Schema(description = "会话ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID（可为空） */
    @Schema(description = "用户ID（可为空）")
    private Long userId;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 客户端设备标识 */
    @Schema(description = "客户端设备标识")
    private String deviceId;

    /** 当前楼层ID */
    @Schema(description = "当前楼层ID")
    private Long floorId;

    /** 最近一次定位坐标（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @Schema(description = "最近一次定位坐标（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口）")
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String lastPos;

    /** 最近一次定位精度（米） */
    @Schema(description = "最近一次定位精度（米）")
    private Double lastAccuracyM;

    /** 会话开始时间 */
    @Schema(description = "会话开始时间")
    private LocalDateTime startTime;

    /** 会话结束时间 */
    @Schema(description = "会话结束时间")
    private LocalDateTime endTime;

    /** 会话状态: ACTIVE=进行中 ENDED=已结束 */
    @Schema(description = "会话状态: ACTIVE=进行中 ENDED=已结束")
    private String status;

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
