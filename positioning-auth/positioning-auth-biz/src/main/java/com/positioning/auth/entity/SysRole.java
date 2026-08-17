package com.positioning.auth.entity;

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
 * 角色表: 定义平台级角色
 */
@Data
@TableName("sys_role")
@Schema(description = "角色表: 定义平台级角色")
public class SysRole {

    /** 角色ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "角色ID（雪花ID，应用层生成）")
    private Long id;

    /** 角色编码: ADMIN/MALL_OPERATOR/MERCHANT/USER */
    @Schema(description = "角色编码: ADMIN/MALL_OPERATOR/MERCHANT/USER")
    private String roleCode;

    /** 角色名称 */
    @Schema(description = "角色名称")
    private String roleName;

    /** 角色描述 */
    @Schema(description = "角色描述")
    private String description;

    /** 角色状态: 1=启用 0=禁用 */
    @Schema(description = "角色状态: 1=启用 0=禁用")
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /** 逻辑删除标记: 0=正常 1=已删除 */
    @TableLogic
    @Schema(description = "逻辑删除标记: 0=正常 1=已删除")
    private Integer deleted;
}
