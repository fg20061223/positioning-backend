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
 * 用户-角色关联表
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户-角色关联表")
public class SysUserRole {

    /** 关联ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "关联ID（雪花ID，应用层生成）")
    private Long id;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 角色ID */
    @Schema(description = "角色ID")
    private Long roleId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
