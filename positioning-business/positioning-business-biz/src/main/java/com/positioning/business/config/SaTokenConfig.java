package com.positioning.business.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由拦截配置: 业务接口全部要求登录（JWT 由认证服务签发）
 * 放行内部 Feign 接口（/internal/**），仅限服务间调用
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
                        .notMatch("/internal/**", "/actuator/**", "/error",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/uploads/**")
                        .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}
