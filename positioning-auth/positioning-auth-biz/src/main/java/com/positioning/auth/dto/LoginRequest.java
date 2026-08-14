package com.positioning.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    /** 账号（支持用户名或手机号） */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
