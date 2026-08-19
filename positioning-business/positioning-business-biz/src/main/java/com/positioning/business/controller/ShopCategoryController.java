package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.ShopCategory;
import com.positioning.business.mapper.ShopCategoryMapper;
import com.positioning.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商铺分类接口
 */
@Tag(name = "商铺分类管理", description = "商铺分类 CRUD（分页/详情/新增/修改/删除）、下拉数据")
@RestController
@RequestMapping("/business/shop-category")
@RequiredArgsConstructor
public class ShopCategoryController extends BaseCrudController<ShopCategory> {

    private final ShopCategoryMapper categoryMapper;

    @Override
    protected BaseMapper<ShopCategory> baseMapper() {
        return categoryMapper;
    }

    /** 商铺分类下拉数据（仅 id + catName, 可按商场过滤, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "商铺分类下拉数据", description = "商铺分类下拉数据（仅 id + name, 可按商场过滤, 入参 {\"mallId\":1}）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(categoryMapper.selectOptions(query));
    }
}
