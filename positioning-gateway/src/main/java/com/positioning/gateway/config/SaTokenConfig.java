package com.positioning.gateway.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关统一鉴权配置（可选增强）
 * <p>
 * 职责: 在网关层对 JWT 做<b>粗校验</b>(是否登录/是否过期), 未登录请求直接 401,
 * 业务服务几乎收不到垃圾请求。细粒度权限(角色码/权限码)仍由各业务服务内部校验(纵深防御)。
 * 认证(签发令牌)仍在 auth 服务, 本配置仅做验签, 与 auth/business 分离设计不冲突。
 * </p>
 * <p>
 * 放行: /auth/login|register(公开)、/internal/**(服务间Feign)、/actuator/**(健康检查)、
 * /fallback/**(熔断降级)、/uploads/**(静态文件)。
 * </p>
 */
@Slf4j
@Configuration
public class SaTokenConfig {

    /** Sa-Token JWT 无状态模式(与认证/业务服务共享 SA_TOKEN_SECRET, 独立验签) */
    @Bean
    public StpLogic stpLogicJwt() {
        StpLogic stpLogic = new StpLogicJwtForStateless();
        StpUtil.setStpLogic(stpLogic);
        log.info("[Sa-Token] 网关 StpLogic 已切换为 JWT 无状态模式: {}", stpLogic.getClass().getSimpleName());
        return stpLogic;
    }

    /** 全局鉴权过滤器: 放行公开路径, 其余统一校验登录态 */
    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(
                        "/auth/login", "/auth/register",  // 认证公开接口
                        "/internal/**",                    // 服务间 Feign 内部接口
                        "/actuator/**", "/error",          // 健康检查
                        "/fallback/**",                    // 熔断降级端点
                        "/uploads/**")                     // 静态文件(楼层平面图)
                .setAuth(obj -> StpUtil.checkLogin())
                .setError(e -> e instanceof NotLoginException
                        ? "{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}"
                        : "{\"code\":500,\"message\":\"系统繁忙，请稍后重试\",\"data\":null}");
    }
}
