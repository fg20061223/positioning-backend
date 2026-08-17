package com.positioning.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI 接口文档配置（认证服务）
 * <p>
 * 所有业务接口统一携带请求头 satoken（JWT）鉴权, 文档内置该安全方案,
 * 在 Swagger UI 右上角 Authorize 中填入登录返回的 satoken 即可调试接口。
 */
@Configuration
public class OpenApiConfig {

    /** 安全方案名称: 对应 Sa-Token 的 token-name */
    private static final String SECURITY_SCHEME_NAME = "satoken";

    @Bean
    public OpenAPI positioningAuthOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("商场可视化智能车位/商铺导航系统 - 认证服务 API")
                        .description("""
                                认证服务接口文档（auth 架构）。
                                接口约定: 全部 POST + JSON, 统一响应 {"code":200,"message":"操作成功","data":...}。
                                除 /auth/login、/auth/register 外均需请求头 satoken。
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("positioning-backend")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("satoken")
                                .description("登录接口返回的 JWT, 形如 eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
