package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.Poi;
import com.positioning.business.mapper.PoiMapper;
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
 * 基础设施/POI 接口
 */
@Tag(name = "基础设施/POI管理", description = "基础设施/POI CRUD（分页/详情/新增/修改/删除）、下拉数据")
@RestController
@RequestMapping("/business/poi")
@RequiredArgsConstructor
public class PoiController extends BaseCrudController<Poi> {

    private final PoiMapper poiMapper;

    @Override
    protected BaseMapper<Poi> baseMapper() {
        return poiMapper;
    }

    /** 设施下拉数据（仅 id + poiName, 可按商场/楼层过滤, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "设施下拉数据", description = "设施下拉数据（仅 id + name, 可按商场/楼层过滤, 入参 {\"mallId\":1,\"floorId\":101}）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(poiMapper.selectOptions(query));
    }
}
