package com.positioning.auth.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户信息 DTO（Feign 跨服务传输）
 */
@Data
@NoArgsConstructor
public class UserDTO implements Serializable {

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

    /** 用户类型: USER/STAFF/ADMIN/MERCHANT */
    private String userType;

    /** 账号状态: 1=启用 0=禁用 */
    private Integer status;
}
