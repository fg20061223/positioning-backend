package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.ShopCategory;
import com.positioning.business.mapper.ShopCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商铺分类接口
 */
@RestController
@RequestMapping("/business/shop-category")
@RequiredArgsConstructor
public class ShopCategoryController extends BaseCrudController<ShopCategory> {

    private final ShopCategoryMapper categoryMapper;

    @Override
    protected BaseMapper<ShopCategory> baseMapper() {
        return categoryMapper;
    }
}
