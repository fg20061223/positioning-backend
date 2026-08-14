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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 蓝牙信标接口
 */
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
    public Result<PageResult<Beacon>> query(@RequestBody(required = false) BeaconQuery request) {
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
    public Result<List<Beacon>> byFloor(@RequestBody IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "楼层ID不能为空");
        }
        return Result.ok(beaconMapper.selectList(Wrappers.lambdaQuery(Beacon.class)
                .eq(Beacon::getFloorId, request.getId())
                .eq(Beacon::getStatus, "ACTIVE")
                .orderByAsc(Beacon::getMinor)));
    }
}
