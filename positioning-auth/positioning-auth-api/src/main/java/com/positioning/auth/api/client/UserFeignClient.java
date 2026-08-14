package com.positioning.auth.api.client;

import com.positioning.auth.api.dto.UserDTO;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户中心 Feign 客户端（供其他微服务调用认证服务查询用户）
 * 实现端: positioning-auth-biz 的 internal/UserFeignController
 */
@FeignClient(name = "positioning-auth", path = "/internal/user")
public interface UserFeignClient {

    /** 按用户ID查询用户（JSON 入参） */
    @PostMapping("/get")
    Result<UserDTO> getUser(@RequestBody IdRequest request);
}
