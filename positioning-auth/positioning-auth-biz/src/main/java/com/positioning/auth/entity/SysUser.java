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
 * 用户表: 平台车主、商场运营、管理员、商户的账号
 */
@Data
@TableName("sys_user")
@Schema(description = "用户表: 平台车主、商场运营、管理员、商户的账号")
public class SysUser {

    /** 用户ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "用户ID（雪花ID，应用层生成）")
    private Long id;

    /** 登录用户名 */
    @Schema(description = "登录用户名")
    private String username;

    /** 登录密码（BCrypt加密哈希） */
    @Schema(description = "登录密码（BCrypt加密哈希）")
    private String passwordHash;

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    private String nickname;

    /** 真实姓名 */
    @Schema(description = "真实姓名")
    private String realName;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** 头像图片地址 */
    @Schema(description = "头像图片地址")
    private String avatarUrl;

    /** 用户类型: USER=普通车主用户 STAFF=商场运营人员 ADMIN=平台管理员 MERCHANT=商铺商户 */
    @Schema(description = "用户类型: USER=普通车主用户 STAFF=商场运营人员 ADMIN=平台管理员 MERCHANT=商铺商户")
    private String userType;

    /** 账号状态: 1=启用 0=禁用 */
    @Schema(description = "账号状态: 1=启用 0=禁用")
    private Integer status;

    /** 最后登录时间 */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

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
