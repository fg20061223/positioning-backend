package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.dto.ZoneQuery;
import com.positioning.business.entity.MallZone;
import com.positioning.business.mapper.MallZoneMapper;
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
 * 分区接口
 */
@Tag(name = "分区管理", description = "分区 CRUD（分页/详情/新增/修改/删除）、条件分页、下拉数据")
@RestController
@RequestMapping("/business/zone")
@RequiredArgsConstructor
public class ZoneController extends BaseCrudController<MallZone> {

    private final MallZoneMapper zoneMapper;

    @Override
    protected BaseMapper<MallZone> baseMapper() {
        return zoneMapper;
    }

    /** 条件分页: 商场/楼层/分区编码/分区名称 */
    @PostMapping("/query")
    @Operation(summary = "条件分页", description = "分区条件分页: 按 商场/楼层 + 分区编码/名称(模糊) 查询")
    public Result<PageResult<MallZone>> query(@RequestBody(required = false) @Parameter(description = "条件分页参数（可空）", required = false) ZoneQuery request) {
        ZoneQuery q = request == null ? new ZoneQuery() : request;
        LambdaQueryWrapper<MallZone> wrapper = Wrappers.lambdaQuery(MallZone.class)
                .eq(q.getMallId() != null, MallZone::getMallId, q.getMallId())
                .eq(q.getFloorId() != null, MallZone::getFloorId, q.getFloorId())
                .like(q.getZoneCode() != null && !q.getZoneCode().isBlank(), MallZone::getZoneCode, q.getZoneCode())
                .like(q.getZoneName() != null && !q.getZoneName().isBlank(), MallZone::getZoneName, q.getZoneName())
                .orderByAsc(MallZone::getSortOrder)
                .orderByAsc(MallZone::getId);
        Page<MallZone> page = zoneMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 分区下拉数据（仅 id + zoneName, 可按商场/楼层过滤, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "分区下拉数据", description = "分区下拉数据（仅 id + name, 可按商场/楼层过滤, 入参 {\"mallId\":1,\"floorId\":101}）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(zoneMapper.selectOptions(query));
    }
}
