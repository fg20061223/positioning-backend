package com.positioning.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** Sa-Token 令牌（JWT） */
    private String token;

    /** 当前用户信息 */
    private UserVO user;
}
