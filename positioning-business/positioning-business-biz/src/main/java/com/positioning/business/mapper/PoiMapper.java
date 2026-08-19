package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.Poi;

import java.util.List;

/**
 * 基础设施/POI Mapper
 */
public interface PoiMapper extends BaseMapper<Poi> {

    /** 设施下拉数据（仅 id + poi_name, 可按商场/楼层过滤, SQL 见 mapper/PoiMapper.xml） */
    List<OptionVO> selectOptions(OptionQuery query);
}
