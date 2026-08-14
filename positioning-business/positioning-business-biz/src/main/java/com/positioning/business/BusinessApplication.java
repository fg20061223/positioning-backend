package com.positioning.business;

import com.positioning.auth.api.client.UserFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 业务服务启动类
 * 职责: 商场/车位/商铺/导航/停车等业务接口（对应 business 架构表）
 * 通过 OpenFeign 调用认证服务查询用户
 */
@SpringBootApplication
@MapperScan("com.positioning.business.mapper")
@EnableFeignClients(clients = UserFeignClient.class)
public class BusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
    }
}
