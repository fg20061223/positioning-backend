package com.positioning.business.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 静态资源映射配置
 * <p>
 * 将 /uploads/** 映射到本地上传目录（positioning.file.upload-dir, 默认 ./uploads）,
 * 供文件上传接口返回的图片 URL 直接访问。
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${positioning.file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + Paths.get(uploadDir).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
