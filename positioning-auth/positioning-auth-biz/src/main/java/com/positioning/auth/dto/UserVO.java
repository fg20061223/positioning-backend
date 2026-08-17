package com.positioning.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前登录用户信息
 */
@Data
@Schema(description = "当前登录用户信息")
public class UserVO {

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long id;

    /** 登录用户名 */
    @Schema(description = "登录用户名")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称")
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

    /** 用户类型: USER/STAFF/ADMIN/MERCHANT */
    @Schema(description = "用户类型: USER/STAFF/ADMIN/MERCHANT")
    private String userType;

    /** 账号状态: 1=启用 0=禁用 */
    @Schema(description = "账号状态: 1=启用 0=禁用")
    private Integer status;

    /** 最后登录时间 */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;
}
