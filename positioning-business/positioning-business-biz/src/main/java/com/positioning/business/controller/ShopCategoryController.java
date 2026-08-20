package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.dto.ShopCategoryQuery;
import com.positioning.business.entity.ShopCategory;
import com.positioning.business.mapper.ShopCategoryMapper;
import com.positioning.common.api.PageResult;
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
@Tag(name = "商铺分类管理", description = "商铺分类 CRUD（分页/详情/新增/修改/删除）、条件分页、下拉数据")
@RestController
@RequestMapping("/business/shop-category")
@RequiredArgsConstructor
public class ShopCategoryController extends BaseCrudController<ShopCategory> {

    private final ShopCategoryMapper categoryMapper;

    @Override
    protected BaseMapper<ShopCategory> baseMapper() {
        return categoryMapper;
    }

    /** 条件分页: 分类名称/所属商场/父分类 */
    @PostMapping("/query")
    @Operation(summary = "条件分页", description = "商铺分类条件分页: 按 分类名称(模糊) + 所属商场 + 父分类 查询")
    public Result<PageResult<ShopCategory>> query(@RequestBody(required = false) @Parameter(description = "条件分页参数（可空）", required = false) ShopCategoryQuery request) {
        ShopCategoryQuery q = request == null ? new ShopCategoryQuery() : request;
        LambdaQueryWrapper<ShopCategory> wrapper = Wrappers.lambdaQuery(ShopCategory.class)
                .like(q.getCatName() != null && !q.getCatName().isBlank(), ShopCategory::getCatName, q.getCatName())
                .eq(q.getMallId() != null, ShopCategory::getMallId, q.getMallId())
                .eq(q.getParentId() != null, ShopCategory::getParentId, q.getParentId())
                .orderByAsc(ShopCategory::getSortOrder)
                .orderByAsc(ShopCategory::getId);
        Page<ShopCategory> page = categoryMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 商铺分类下拉数据（仅 id + catName, 可按商场过滤, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "商铺分类下拉数据", description = "商铺分类下拉数据（仅 id + name, 可按商场过滤, 入参 {\"mallId\":1}）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(categoryMapper.selectOptions(query));
    }
}
