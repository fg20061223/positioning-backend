package com.positioning.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停车记录表: 用户停车入场/离场记录
 */
@Schema(description = "停车记录表: 用户停车入场/离场记录")
@Data
@TableName("parking_record")
public class ParkingRecord {

    /** 记录ID（雪花ID，应用层生成） */
    @Schema(description = "记录ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID（可为空，支持未登录录入） */
    @Schema(description = "用户ID（可为空，支持未登录录入）")
    private Long userId;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 车位ID */
    @Schema(description = "车位ID")
    private Long spaceId;

    /** 车牌号 */
    @Schema(description = "车牌号")
    private String plateNo;

    /** 入场时间 */
    @Schema(description = "入场时间")
    private LocalDateTime entryTime;

    /** 离场时间 */
    @Schema(description = "离场时间")
    private LocalDateTime exitTime;

    /** 记录状态: PARKING=停车中 ENDED=已结束 CANCELLED=已取消 */
    @Schema(description = "记录状态: PARKING=停车中 ENDED=已结束 CANCELLED=已取消")
    private String status;

    /** 录入来源: APP=小程序 PLATE=车牌识别 STAFF=人工 */
    @Schema(description = "录入来源: APP=小程序 PLATE=车牌识别 STAFF=人工")
    private String source;

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
