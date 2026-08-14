package com.positioning.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证服务启动类
 * 职责: Sa-Token 登录鉴权、RBAC 权限、用户中心接口（对应 auth 架构表）
 */
@SpringBootApplication
@MapperScan("com.positioning.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
