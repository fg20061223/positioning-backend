package com.positioning.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类
 * 职责: 路由转发(Spring Cloud Gateway)、限流(Caffeine 令牌桶)、熔断降级(Resilience4j)
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
