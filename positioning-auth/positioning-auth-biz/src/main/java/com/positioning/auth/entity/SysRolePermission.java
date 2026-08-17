package com.positioning.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-权限关联表
 */
@Data
@TableName("sys_role_permission")
@Schema(description = "角色-权限关联表")
public class SysRolePermission {

    /** 关联ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "关联ID（雪花ID，应用层生成）")
    private Long id;

    /** 角色ID */
    @Schema(description = "角色ID")
    private Long roleId;

    /** 权限ID */
    @Schema(description = "权限ID")
    private Long permissionId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
