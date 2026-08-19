package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.ShopCategory;

import java.util.List;

/**
 * 商铺分类 Mapper
 */
public interface ShopCategoryMapper extends BaseMapper<ShopCategory> {

    /** 商铺分类下拉数据（仅 id + cat_name, 可按商场过滤, SQL 见 mapper/ShopCategoryMapper.xml） */
    List<OptionVO> selectOptions(OptionQuery query);
}
