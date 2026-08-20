package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.MallQuery;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.Mall;
import com.positioning.business.mapper.MallMapper;
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
 * 商场接口
 */
@Tag(name = "商场管理", description = "商场 CRUD（分页/详情/新增/修改/删除）、条件分页、下拉数据")
@RestController
@RequestMapping("/business/mall")
@RequiredArgsConstructor
public class MallController extends BaseCrudController<Mall> {

    private final MallMapper mallMapper;

    @Override
    protected BaseMapper<Mall> baseMapper() {
        return mallMapper;
    }

    /** 条件分页: 商场编码/名称/省份/城市/区县/详细地址/状态 */
    @PostMapping("/query")
    @Operation(summary = "条件分页", description = "商场条件分页: 按 商场编码/名称/省份/城市/区县/详细地址(模糊) + 状态 查询")
    public Result<PageResult<Mall>> query(@RequestBody(required = false) @Parameter(description = "条件分页参数（可空）", required = false) MallQuery request) {
        MallQuery q = request == null ? new MallQuery() : request;
        LambdaQueryWrapper<Mall> wrapper = Wrappers.lambdaQuery(Mall.class)
                .like(q.getMallCode() != null && !q.getMallCode().isBlank(), Mall::getMallCode, q.getMallCode())
                .like(q.getMallName() != null && !q.getMallName().isBlank(), Mall::getMallName, q.getMallName())
                .eq(q.getProvince() != null && !q.getProvince().isBlank(), Mall::getProvince, q.getProvince())
                .eq(q.getCity() != null && !q.getCity().isBlank(), Mall::getCity, q.getCity())
                .eq(q.getDistrict() != null && !q.getDistrict().isBlank(), Mall::getDistrict, q.getDistrict())
                .like(q.getAddress() != null && !q.getAddress().isBlank(), Mall::getAddress, q.getAddress())
                .eq(q.getStatus() != null, Mall::getStatus, q.getStatus())
                .orderByAsc(Mall::getId);
        Page<Mall> page = mallMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 商场下拉数据（仅 id + mallName, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "商场下拉数据", description = "商场下拉数据（仅 id + name, 供下拉框使用）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(mallMapper.selectOptions(query));
    }
}
