package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.MallFloor;
import com.positioning.business.mapper.MallFloorMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import com.positioning.common.exception.BizException;
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
 * 楼层接口
 */
@Tag(name = "楼层管理", description = "楼层 CRUD、按商场查询楼层列表")
@RestController
@RequestMapping("/business/floor")
@RequiredArgsConstructor
public class FloorController extends BaseCrudController<MallFloor> {

    private final MallFloorMapper floorMapper;

    @Override
    protected BaseMapper<MallFloor> baseMapper() {
        return floorMapper;
    }

    /** 按商场查询楼层列表（按排序号升序, JSON: {"id":商场ID}） */
    @PostMapping("/by-mall")
    @Operation(summary = "按商场查询楼层列表", description = "按商场查询楼层列表（按排序号升序, 入参 {\"id\":商场ID}）")
    public Result<List<MallFloor>> byMall(@RequestBody @Parameter(description = "按ID操作参数（商场ID）", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "商场ID不能为空");
        }
        return Result.ok(floorMapper.selectList(Wrappers.lambdaQuery(MallFloor.class)
                .eq(MallFloor::getMallId, request.getId())
                .orderByAsc(MallFloor::getSortOrder)));
    }

    /** 楼层下拉数据（仅 id + floorName, 可按商场过滤, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "楼层下拉数据", description = "楼层下拉数据（仅 id + name, 可按商场过滤, 入参 {\"mallId\":1}）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(floorMapper.selectOptions(query));
    }
}
