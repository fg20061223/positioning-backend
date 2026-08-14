package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.FloorConnect;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 跨楼层连接 Mapper
 */
public interface FloorConnectMapper extends BaseMapper<FloorConnect> {

    /** 查询某商场所有可用跨层连接（路径规划用） */
    @Select("""
            SELECT id, mall_id, from_floor_id, to_floor_id, connect_type, from_node_id, to_node_id, cost_m, status
            FROM floor_connect
            WHERE mall_id = #{mallId}
              AND status = 1
              AND deleted = 0
            """)
    List<FloorConnect> selectEnabledByMall(@Param("mallId") Long mallId);
}
