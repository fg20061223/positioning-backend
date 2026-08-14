package com.positioning.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.positioning.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源: 从 auth 架构的 RBAC 表实时加载角色与权限
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;

    /** 返回用户权限码列表 */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return sysUserMapper.selectPermissionCodes(toLong(loginId));
    }

    /** 返回用户角色编码列表 */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return sysUserMapper.selectRoleCodes(toLong(loginId));
    }

    private Long toLong(Object loginId) {
        return Long.valueOf(loginId.toString());
    }
}
