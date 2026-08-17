package com.positioning.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /** 账号（支持用户名或手机号） */
    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号（支持用户名或手机号）")
    private String account;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码")
    private String password;
}
