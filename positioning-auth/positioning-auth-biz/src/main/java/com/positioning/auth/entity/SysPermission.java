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
 * 权限表: 菜单/按钮/接口三级权限点
 */
@Data
@TableName("sys_permission")
@Schema(description = "权限表: 菜单/按钮/接口三级权限点")
public class SysPermission {

    /** 权限ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "权限ID（雪花ID，应用层生成）")
    private Long id;

    /** 父权限ID，0=根权限 */
    @Schema(description = "父权限ID，0=根权限")
    private Long parentId;

    /** 权限编码 */
    @Schema(description = "权限编码")
    private String permCode;

    /** 权限名称 */
    @Schema(description = "权限名称")
    private String permName;

    /** 权限类型: MENU=菜单 BUTTON=按钮 API=接口 */
    @Schema(description = "权限类型: MENU=菜单 BUTTON=按钮 API=接口")
    private String permType;

    /** 资源路径 */
    @Schema(description = "资源路径")
    private String path;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 逻辑删除标记: 0=正常 1=已删除 */
    @TableLogic
    @Schema(description = "逻辑删除标记: 0=正常 1=已删除")
    private Integer deleted;
}
