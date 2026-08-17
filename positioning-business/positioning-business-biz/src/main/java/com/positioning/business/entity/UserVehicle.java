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
 * 用户车辆表: 维护用户常用车牌
 */
@Schema(description = "用户车辆表: 维护用户常用车牌")
@Data
@TableName("user_vehicle")
public class UserVehicle {

    /** 车辆ID（雪花ID，应用层生成） */
    @Schema(description = "车辆ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属用户ID */
    @Schema(description = "所属用户ID")
    private Long userId;

    /** 车牌号 */
    @Schema(description = "车牌号")
    private String plateNo;

    /** 是否默认车辆: true=是 false=否 */
    @Schema(description = "是否默认车辆: true=是 false=否")
    private Boolean isDefault;

    /** 车辆状态: 1=正常 0=失效 */
    @Schema(description = "车辆状态: 1=正常 0=失效")
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
