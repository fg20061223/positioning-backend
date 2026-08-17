package com.positioning.auth.controller.internal;

import com.positioning.auth.api.client.UserFeignClient;
import com.positioning.auth.api.dto.UserDTO;
import com.positioning.auth.entity.SysUser;
import com.positioning.auth.mapper.SysUserMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心内部接口实现（供 Feign 跨服务调用）
 */
@Tag(name = "用户中心内部接口", description = "供 Feign 跨服务调用的用户信息接口")
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class UserFeignController implements UserFeignClient {

    private final SysUserMapper sysUserMapper;

    @Override
    @Operation(summary = "获取用户信息", description = "按用户ID查询用户信息，供 Feign 跨服务调用")
    public Result<UserDTO> getUser(@Parameter(description = "按ID查询用户请求", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "用户ID不能为空");
        }
        SysUser user = sysUserMapper.selectById(request.getId());
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setUserType(user.getUserType());
        dto.setStatus(user.getStatus());
        return Result.ok(dto);
    }
}
