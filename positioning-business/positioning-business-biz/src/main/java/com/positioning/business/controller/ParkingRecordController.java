package com.positioning.business.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.entity.ParkingRecord;
import com.positioning.business.mapper.ParkingRecordMapper;
import com.positioning.business.service.ParkingRecordService;
import com.positioning.common.api.PageResult;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import com.positioning.common.dto.PageQuery;
import com.positioning.common.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 停车记录接口
 */
@Tag(name = "停车记录管理", description = "停车记录 CRUD、入场停车、离场、我的停车记录")
@RestController
@RequestMapping("/business/parking-record")
@RequiredArgsConstructor
public class ParkingRecordController extends BaseCrudController<ParkingRecord> {

    private final ParkingRecordMapper parkingRecordMapper;
    private final ParkingRecordService parkingRecordService;

    @Override
    protected BaseMapper<ParkingRecord> baseMapper() {
        return parkingRecordMapper;
    }

    /** 入场停车（联动车位占用） */
    @PostMapping("/park")
    @Operation(summary = "入场停车", description = "入场停车（联动车位占用）")
    public Result<ParkingRecord> park(@RequestBody @Parameter(description = "停车记录实体", required = true) ParkingRecord record) {
        if (record.getUserId() == null) {
            record.setUserId(StpUtil.getLoginIdAsLong());
        }
        return Result.ok(parkingRecordService.park(record));
    }

    /** 离场（联动车位释放, JSON: {"id":记录ID}） */
    @PostMapping("/end")
    @Operation(summary = "离场", description = "离场（联动车位释放, 入参 {\"id\":记录ID}）")
    public Result<ParkingRecord> end(@RequestBody @Parameter(description = "按ID操作参数（记录ID）", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "记录ID不能为空");
        }
        return Result.ok(parkingRecordService.end(request.getId()));
    }

    /** 我的停车记录 */
    @PostMapping("/my")
    @Operation(summary = "我的停车记录", description = "我的停车记录（当前登录用户分页）")
    public Result<PageResult<ParkingRecord>> my(@RequestBody(required = false) @Parameter(description = "分页参数（可空）", required = false) PageQuery query) {
        PageQuery q = query == null ? new PageQuery() : query;
        Long userId = StpUtil.getLoginIdAsLong();
        Page<ParkingRecord> page = parkingRecordMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                Wrappers.lambdaQuery(ParkingRecord.class)
                        .eq(ParkingRecord::getUserId, userId)
                        .orderByDesc(ParkingRecord::getEntryTime));
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }
}
