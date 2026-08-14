package com.positioning.gateway.controller;

import com.positioning.common.api.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 熔断降级兜底接口: 下游服务不可用时返回统一降级信息
 */
@RestController
public class FallbackController {

    /** 认证服务熔断降级 */
    @PostMapping("/fallback/auth")
    public Result<Void> authFallback() {
        return Result.fail(503, "认证服务暂不可用，请稍后重试");
    }

    /** 业务服务熔断降级 */
    @PostMapping("/fallback/business")
    public Result<Void> businessFallback() {
        return Result.fail(503, "业务服务暂不可用，请稍后重试");
    }
}
