package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.Mall;
import com.positioning.business.mapper.MallMapper;
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
@Tag(name = "商场管理", description = "商场 CRUD（分页/详情/新增/修改/删除）、下拉数据")
@RestController
@RequestMapping("/business/mall")
@RequiredArgsConstructor
public class MallController extends BaseCrudController<Mall> {

    private final MallMapper mallMapper;

    @Override
    protected BaseMapper<Mall> baseMapper() {
        return mallMapper;
    }

    /** 商场下拉数据（仅 id + mallName, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "商场下拉数据", description = "商场下拉数据（仅 id + name, 供下拉框使用）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(mallMapper.selectOptions(query));
    }
}
