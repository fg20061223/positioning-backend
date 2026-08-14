package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.NavEdge;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 导航边 Mapper
 */
public interface NavEdgeMapper extends BaseMapper<NavEdge> {

    /** 查询某商场所有可用边（路径规划用） */
    @Select("""
            SELECT id, mall_id, from_node_id, to_node_id, edge_type, distance_m, weight, bidirectional, status
            FROM nav_edge
            WHERE mall_id = #{mallId}
              AND status = 1
              AND deleted = 0
            """)
    List<NavEdge> selectEnabledByMall(@Param("mallId") Long mallId);
}
