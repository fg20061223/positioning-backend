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
 * 系统字典表: 记录原表结构设计的枚举取值（下拉框/展示文案用）
 */
@Schema(description = "系统字典表: 记录原表结构设计的枚举取值")
@Data
@TableName("sys_dict")
public class SysDict {

    /** 字典ID（雪花ID，应用层生成） */
    @Schema(description = "字典ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 字典类型编码，如 space_type/space_status/shop_status */
    @Schema(description = "字典类型编码，如 space_type/space_status/shop_status")
    private String dictType;

    /** 字典项编码（对应字段存储值） */
    @Schema(description = "字典项编码（对应字段存储值）")
    private String dictCode;

    /** 字典项名称（中文展示文案） */
    @Schema(description = "字典项名称（中文展示文案）")
    private String dictLabel;

    /** 排序号（类型内升序） */
    @Schema(description = "排序号（类型内升序）")
    private Integer sortOrder;

    /** 状态: 1=启用 0=停用 */
    @Schema(description = "状态: 1=启用 0=停用")
    private Integer status;

    /** 备注（可标注来源表/字段） */
    @Schema(description = "备注（可标注来源表/字段）")
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
