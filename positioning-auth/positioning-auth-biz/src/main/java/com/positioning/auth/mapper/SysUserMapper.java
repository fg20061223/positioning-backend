package com.positioning.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.auth.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 查询用户角色编码列表 */
    @Select("""
            SELECT r.role_code
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
              AND r.deleted = 0
            """)
    List<String> selectRoleCodes(@Param("userId") Long userId);

    /** 查询用户权限编码列表 */
    @Select("""
            SELECT DISTINCT p.perm_code
            FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            JOIN sys_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId}
              AND p.deleted = 0
              AND p.perm_code IS NOT NULL
            """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);
}
