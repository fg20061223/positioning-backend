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
 * 用户收藏表: 常去车位、商铺、设施
 */
@Schema(description = "用户收藏表: 常去车位、商铺、设施")
@Data
@TableName("user_destination")
public class UserDestination {

    /** 收藏ID（雪花ID，应用层生成） */
    @Schema(description = "收藏ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 目标类型: SPACE=车位 SHOP=商铺 POI=设施 */
    @Schema(description = "目标类型: SPACE=车位 SHOP=商铺 POI=设施")
    private String targetType;

    /** 目标ID（对应目标类型的主键） */
    @Schema(description = "目标ID（对应目标类型的主键）")
    private Long targetId;

    /** 收藏别名 */
    @Schema(description = "收藏别名")
    private String aliasName;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sortOrder;

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
