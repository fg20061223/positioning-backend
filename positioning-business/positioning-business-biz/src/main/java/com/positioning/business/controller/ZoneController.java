package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.OptionQuery;
import com.positioning.business.dto.OptionVO;
import com.positioning.business.entity.MallZone;
import com.positioning.business.mapper.MallZoneMapper;
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
@Tag(name = "分区管理", description = "分区 CRUD（分页/详情/新增/修改/删除）、下拉数据")
@RestController
@RequestMapping("/business/zone")
@RequiredArgsConstructor
public class ZoneController extends BaseCrudController<MallZone> {

    private final MallZoneMapper zoneMapper;

    @Override
    protected BaseMapper<MallZone> baseMapper() {
        return zoneMapper;
    }

    /** 分区下拉数据（仅 id + zoneName, 可按商场/楼层过滤, 供下拉框） */
    @PostMapping("/options")
    @Operation(summary = "分区下拉数据", description = "分区下拉数据（仅 id + name, 可按商场/楼层过滤, 入参 {\"mallId\":1,\"floorId\":101}）")
    public Result<List<OptionVO>> options(@RequestBody(required = false) @Parameter(description = "查询条件（可空）", required = false) OptionQuery query) {
        return Result.ok(zoneMapper.selectOptions(query));
    }
}
