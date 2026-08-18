package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.NavEdge;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导航边 Mapper
 */
public interface NavEdgeMapper extends BaseMapper<NavEdge> {

    /** 查询某商场所有可用边（路径规划用；SQL 见 mapper/NavEdgeMapper.xml） */
    List<NavEdge> selectEnabledByMall(@Param("mallId") Long mallId);
}
