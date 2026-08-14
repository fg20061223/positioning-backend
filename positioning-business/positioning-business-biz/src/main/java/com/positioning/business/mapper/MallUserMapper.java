package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.MallUser;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-商场绑定 Mapper
 */
public interface MallUserMapper extends BaseMapper<MallUser> {

    /** 查询用户在商场内的角色编码列表 */
    @Select("""
            SELECT role_code
            FROM mall_user
            WHERE user_id = #{userId}
              AND status = 1
              AND deleted = 0
            """)
    List<String> selectRoleCodesByUserId(Long userId);
}
