package com.positioning.business.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.positioning.common.mybatis.PostgresTimestamptzTypeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 类型处理器注册配置
 * <p>
 * 编程式注册 PostgreSQL TIMESTAMPTZ -&gt; LocalDateTime 类型处理器,
 * 与 application.yml 中 mybatis-plus.type-handlers-package 双保险,
 * 确保实体 LocalDateTime 字段读取 TIMESTAMPTZ 列时不抛类型转换异常。
 */
@Configuration
public class MybatisTypeHandlerConfig {

    @Bean
    public ConfigurationCustomizer postgresTimestamptzTypeHandlerCustomizer() {
        return configuration -> configuration.getTypeHandlerRegistry()
                .register(PostgresTimestamptzTypeHandler.class);
    }
}
