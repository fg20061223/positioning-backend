package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.positioning.business.dto.FloorByMallQuery;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.MallFloor;
import com.positioning.business.mapper.MallFloorMapper;
import com.positioning.common.api.Result;
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
@Tag(name = "楼层管理", description = "楼层 CRUD、按商场查询楼层列表（支持条件）、下拉数据")
@RestController
@RequestMapping("/business/floor")
@RequiredArgsConstructor
public class FloorController extends BaseCrudController<MallFloor> {

    private final MallFloorMapper floorMapper;

    @Override
    protected BaseMapper<MallFloor> baseMapper() {
        return floorMapper;
    }

    /** 按商场查询楼层列表（可按 楼层编码/名称/排序号 过滤, 兼容旧入参 {"id":商场ID}） */
    @PostMapping("/by-mall")
    @Operation(summary = "按商场查询楼层列表", description = "按商场查询楼层列表: 入参 {\"mallId\":1}（兼容 {\"id\":1}）, 可选 楼层编码/名称(模糊)、排序号(精确); 按排序号升序")
    public Result<List<MallFloor>> byMall(@RequestBody(required = false) @Parameter(description = "楼层列表查询请求（商场ID必填, 其余可选）", required = true) FloorByMallQuery request) {
        Long mallId = request == null ? null : request.getMallId();
        if (mallId == null && request != null) {
            mallId = request.getId();
        }
        if (mallId == null) {
            throw new BizException(400, "商场ID不能为空");
        }
        LambdaQueryWrapper<MallFloor> wrapper = Wrappers.lambdaQuery(MallFloor.class)
                .eq(MallFloor::getMallId, mallId)
                .like(request.getFloorCode() != null && !request.getFloorCode().isBlank(), MallFloor::getFloorCode, request.getFloorCode())
                .like(request.getFloorName() != null && !request.getFloorName().isBlank(), MallFloor::getFloorName, request.getFloorName())
                .eq(request.getSortOrder() != null, MallFloor::getSortOrder, request.getSortOrder())
                .orderByAsc(MallFloor::getSortOrder)
                .orderByAsc(MallFloor::getId);
        return Result.ok(floorMapper.selectList(wrapper));
    }

    /** 楼层下拉数据（仅 id + floorName, 可按商场过滤, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "楼层下拉数据", description = "楼层下拉数据（仅 id + name, 可按商场过滤, 入参 {\"mallId\":1}）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(floorMapper.selectOptions(query));
    }
}
