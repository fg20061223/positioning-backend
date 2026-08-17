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
 * 商铺分类表
 */
@Schema(description = "商铺分类表")
@Data
@TableName("shop_category")
public class ShopCategory {

    /** 分类ID（雪花ID，应用层生成） */
    @Schema(description = "分类ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID（空=平台通用分类） */
    @Schema(description = "所属商场ID（空=平台通用分类）")
    private Long mallId;

    /** 父分类ID，0=根分类 */
    @Schema(description = "父分类ID，0=根分类")
    private Long parentId;

    /** 分类名称 */
    @Schema(description = "分类名称")
    private String catName;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /** 分类状态: 1=启用 0=停用 */
    @Schema(description = "分类状态: 1=启用 0=停用")
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
