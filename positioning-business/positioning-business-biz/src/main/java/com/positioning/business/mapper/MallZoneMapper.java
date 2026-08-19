package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.MallZone;

import java.util.List;

/**
 * 分区 Mapper
 */
public interface MallZoneMapper extends BaseMapper<MallZone> {

    /** 分区下拉数据（仅 id + zone_name, 可按商场/楼层过滤, SQL 见 mapper/MallZoneMapper.xml） */
    List<OptionVO> selectOptions(OptionQuery query);
}
