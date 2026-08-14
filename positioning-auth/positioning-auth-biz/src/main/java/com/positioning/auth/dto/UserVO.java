package com.positioning.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前登录用户信息
 */
@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像图片地址 */
    private String avatarUrl;

    /** 用户类型: USER/STAFF/ADMIN/MERCHANT */
    private String userType;

    /** 账号状态: 1=启用 0=禁用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;
}
