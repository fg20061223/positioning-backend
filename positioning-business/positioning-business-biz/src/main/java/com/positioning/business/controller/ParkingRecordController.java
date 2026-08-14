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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 停车记录接口
 */
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
    public Result<ParkingRecord> park(@RequestBody ParkingRecord record) {
        if (record.getUserId() == null) {
            record.setUserId(StpUtil.getLoginIdAsLong());
        }
        return Result.ok(parkingRecordService.park(record));
    }

    /** 离场（联动车位释放, JSON: {"id":记录ID}） */
    @PostMapping("/end")
    public Result<ParkingRecord> end(@RequestBody IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "记录ID不能为空");
        }
        return Result.ok(parkingRecordService.end(request.getId()));
    }

    /** 我的停车记录 */
    @PostMapping("/my")
    public Result<PageResult<ParkingRecord>> my(@RequestBody(required = false) PageQuery query) {
        PageQuery q = query == null ? new PageQuery() : query;
        Long userId = StpUtil.getLoginIdAsLong();
        Page<ParkingRecord> page = parkingRecordMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                Wrappers.lambdaQuery(ParkingRecord.class)
                        .eq(ParkingRecord::getUserId, userId)
                        .orderByDesc(ParkingRecord::getEntryTime));
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }
}
