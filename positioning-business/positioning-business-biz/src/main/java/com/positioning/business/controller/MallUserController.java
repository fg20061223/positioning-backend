package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.auth.api.client.UserFeignClient;
import com.positioning.auth.api.dto.UserDTO;
import com.positioning.business.entity.MallUser;
import com.positioning.business.mapper.MallUserMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import com.positioning.common.exception.BizException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户-商场绑定接口（演示 OpenFeign 跨服务调用认证中心）
 */
@RestController
@RequestMapping("/business/mall-user")
@RequiredArgsConstructor
public class MallUserController extends BaseCrudController<MallUser> {

    private final MallUserMapper mallUserMapper;
    private final UserFeignClient userFeignClient;

    @Override
    protected BaseMapper<MallUser> baseMapper() {
        return mallUserMapper;
    }

    /** 新增绑定前通过 Feign 校验用户存在 */
    @Override
    @PostMapping("/create")
    public Result<MallUser> create(@RequestBody MallUser mallUser) {
        try {
            Result<UserDTO> userResult = userFeignClient.getUser(new IdRequest(mallUser.getUserId()));
            if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
                throw new BizException(404, "用户不存在");
            }
        } catch (FeignException e) {
            throw new BizException(503, "认证服务暂不可用");
        }
        mallUserMapper.insert(mallUser);
        return Result.ok(mallUser);
    }

    /** 绑定详情 + 用户信息（跨服务聚合, JSON: {"id":绑定ID}） */
    @PostMapping("/user-info")
    public Result<Map<String, Object>> userInfo(@RequestBody IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "绑定ID不能为空");
        }
        MallUser mallUser = mallUserMapper.selectById(request.getId());
        if (mallUser == null) {
            throw new BizException(404, "绑定记录不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("mallUser", mallUser);
        try {
            Result<UserDTO> userResult = userFeignClient.getUser(new IdRequest(mallUser.getUserId()));
            data.put("user", userResult == null ? null : userResult.getData());
        } catch (Exception e) {
            data.put("user", null);
        }
        return Result.ok(data);
    }
}
