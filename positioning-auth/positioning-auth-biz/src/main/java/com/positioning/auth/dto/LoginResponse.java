package com.positioning.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /** Sa-Token 令牌（JWT） */
    @Schema(description = "Sa-Token 令牌（JWT）")
    private String token;

    /** 当前用户信息 */
    @Schema(description = "当前用户信息")
    private UserVO user;
}
