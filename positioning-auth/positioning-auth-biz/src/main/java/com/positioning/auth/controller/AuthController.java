package com.positioning.auth.controller;

import com.positioning.auth.dto.LoginRequest;
import com.positioning.auth.dto.LoginResponse;
import com.positioning.auth.dto.RegisterRequest;
import com.positioning.auth.dto.UserVO;
import com.positioning.auth.service.AuthService;
import com.positioning.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口: 登录、注册、退出、当前用户
 */
@Tag(name = "认证管理", description = "注册/登录/登出/当前用户")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录 */
    @Operation(summary = "登录", description = "账号密码登录，成功后返回令牌与当前用户信息")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid @Parameter(description = "登录请求", required = true) LoginRequest request, HttpServletRequest httpRequest) {
        return Result.ok(authService.login(request, clientIp(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    /** 注册 */
    @Operation(summary = "注册", description = "注册新账号，成功后返回用户ID")
    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid @Parameter(description = "注册请求", required = true) RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    /** 退出登录 */
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    /** 当前登录用户信息 */
    @Operation(summary = "当前登录用户信息", description = "获取当前登录用户的信息")
    @PostMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.me());
    }

    /** 获取客户端IP（兼容网关转发） */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
