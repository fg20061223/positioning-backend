package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.MallFloor;

import java.util.List;

/**
 * 楼层 Mapper
 */
public interface MallFloorMapper extends BaseMapper<MallFloor> {

    /** 楼层下拉数据（仅 id + floor_name, 可按商场过滤, SQL 见 mapper/MallFloorMapper.xml） */
    List<OptionVO> selectOptions(OptionQuery query);
}
