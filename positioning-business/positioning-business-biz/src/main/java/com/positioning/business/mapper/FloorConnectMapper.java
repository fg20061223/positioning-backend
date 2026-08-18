package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.FloorConnect;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 跨楼层连接 Mapper
 */
public interface FloorConnectMapper extends BaseMapper<FloorConnect> {

    /** 查询某商场所有可用跨层连接（路径规划用；SQL 见 mapper/FloorConnectMapper.xml） */
    List<FloorConnect> selectEnabledByMall(@Param("mallId") Long mallId);
}
