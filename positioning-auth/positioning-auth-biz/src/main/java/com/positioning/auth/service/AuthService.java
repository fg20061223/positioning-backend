package com.positioning.auth.service;

import com.positioning.auth.dto.LoginRequest;
import com.positioning.auth.dto.LoginResponse;
import com.positioning.auth.dto.RegisterRequest;
import com.positioning.auth.dto.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /** 登录 */
    LoginResponse login(LoginRequest request, String ip, String userAgent);

    /** 退出登录 */
    void logout();

    /** 当前登录用户信息 */
    UserVO me();

    /** 注册 */
    Long register(RegisterRequest request);
}
