package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.ShopCategory;
import com.positioning.business.mapper.ShopCategoryMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商铺分类接口
 */
@Tag(name = "商铺分类管理", description = "商铺分类 CRUD（分页/详情/新增/修改/删除）")
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
