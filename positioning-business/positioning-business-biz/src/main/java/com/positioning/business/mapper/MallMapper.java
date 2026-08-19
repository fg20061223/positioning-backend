package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.Mall;

import java.util.List;

/**
 * 商场 Mapper
 */
public interface MallMapper extends BaseMapper<Mall> {

    /** 商场下拉数据（仅 id + mall_name, SQL 见 mapper/MallMapper.xml） */
    List<OptionVO> selectOptions(OptionQuery query);
}
