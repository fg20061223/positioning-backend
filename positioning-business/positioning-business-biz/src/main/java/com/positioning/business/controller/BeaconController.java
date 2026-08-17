package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.BeaconQuery;
import com.positioning.business.entity.Beacon;
import com.positioning.business.mapper.BeaconMapper;
import com.positioning.common.api.PageResult;
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
 * 蓝牙信标接口
 */
@Tag(name = "蓝牙信标管理", description = "信标 CRUD、条件分页、按楼层查询正常信标")
@RestController
@RequestMapping("/business/beacon")
@RequiredArgsConstructor
public class BeaconController extends BaseCrudController<Beacon> {

    private final BeaconMapper beaconMapper;

    @Override
    protected BaseMapper<Beacon> baseMapper() {
        return beaconMapper;
    }

    /** 条件分页: 商场/楼层/状态 */
    @PostMapping("/query")
    @Operation(summary = "条件分页", description = "信标条件分页: 商场/楼层/状态")
    public Result<PageResult<Beacon>> query(@RequestBody(required = false) @Parameter(description = "条件分页参数（可空）", required = false) BeaconQuery request) {
        BeaconQuery q = request == null ? new BeaconQuery() : request;
        LambdaQueryWrapper<Beacon> wrapper = Wrappers.lambdaQuery(Beacon.class)
                .eq(q.getMallId() != null, Beacon::getMallId, q.getMallId())
                .eq(q.getFloorId() != null, Beacon::getFloorId, q.getFloorId())
                .eq(q.getStatus() != null && !q.getStatus().isBlank(), Beacon::getStatus, q.getStatus())
                .orderByAsc(Beacon::getFloorId)
                .orderByAsc(Beacon::getMinor);
        Page<Beacon> page = beaconMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 按楼层查询正常信标列表（定位扫描用, JSON: {"id":楼层ID}） */
    @PostMapping("/by-floor")
    @Operation(summary = "按楼层查询正常信标", description = "按楼层查询正常信标列表（定位扫描用, 入参 {\"id\":楼层ID}）")
    public Result<List<Beacon>> byFloor(@RequestBody @Parameter(description = "按ID操作参数（楼层ID）", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "楼层ID不能为空");
        }
        return Result.ok(beaconMapper.selectList(Wrappers.lambdaQuery(Beacon.class)
                .eq(Beacon::getFloorId, request.getId())
                .eq(Beacon::getStatus, "ACTIVE")
                .orderByAsc(Beacon::getMinor)));
    }
}
