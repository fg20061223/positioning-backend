package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.MallUser;

import java.util.List;

/**
 * 用户-商场绑定 Mapper
 */
public interface MallUserMapper extends BaseMapper<MallUser> {

    /** 查询用户在商场内的角色编码列表（SQL 见 mapper/MallUserMapper.xml） */
    List<String> selectRoleCodesByUserId(Long userId);
}
