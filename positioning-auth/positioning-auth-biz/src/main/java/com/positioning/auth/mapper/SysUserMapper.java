package com.positioning.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.auth.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 查询用户角色编码列表（SQL 见 mapper/SysUserMapper.xml） */
    List<String> selectRoleCodes(@Param("userId") Long userId);

    /** 查询用户权限编码列表（SQL 见 mapper/SysUserMapper.xml） */
    List<String> selectPermissionCodes(@Param("userId") Long userId);
}
