package com.positioning.business.config;

import cn.dev33.satoken.stp.StpInterface;
import com.positioning.business.mapper.MallUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源: 业务侧角色取自 mall_user（商场维度角色），权限码暂为空
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final MallUserMapper mallUserMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return mallUserMapper.selectRoleCodesByUserId(Long.valueOf(loginId.toString()));
    }
}
