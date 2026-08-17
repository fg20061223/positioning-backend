package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.NearbyRequest;
import com.positioning.business.dto.NearbySpaceVO;
import com.positioning.business.dto.OccupyRequest;
import com.positioning.business.dto.SpaceGeometryRequest;
import com.positioning.business.dto.SpaceGeometryVO;
import com.positioning.business.dto.SpaceQuery;
import com.positioning.business.dto.SpaceSearchRequest;
import com.positioning.business.entity.ParkingSpace;
import com.positioning.business.mapper.ParkingSpaceMapper;
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
 * 车位接口（含 PostGIS 空间查询; 全部 POST + JSON）
 */
@Tag(name = "车位管理", description = "车位 CRUD、条件分页、车位号搜索、附近空闲车位、几何读写、占用释放")
@RestController
@RequestMapping("/business/space")
@RequiredArgsConstructor
public class SpaceController extends BaseCrudController<ParkingSpace> {

    private final ParkingSpaceMapper spaceMapper;

    @Override
    protected BaseMapper<ParkingSpace> baseMapper() {
        return spaceMapper;
    }

    /** 条件分页: 商场/楼层/状态 */
    @PostMapping("/query")
    @Operation(summary = "条件分页", description = "车位条件分页: 商场/楼层/状态")
    public Result<PageResult<ParkingSpace>> query(@RequestBody(required = false) @Parameter(description = "条件分页参数（可空）", required = false) SpaceQuery request) {
        SpaceQuery q = request == null ? new SpaceQuery() : request;
        LambdaQueryWrapper<ParkingSpace> wrapper = Wrappers.lambdaQuery(ParkingSpace.class)
                .eq(q.getMallId() != null, ParkingSpace::getMallId, q.getMallId())
                .eq(q.getFloorId() != null, ParkingSpace::getFloorId, q.getFloorId())
                .eq(q.getStatus() != null && !q.getStatus().isBlank(), ParkingSpace::getStatus, q.getStatus())
                .orderByAsc(ParkingSpace::getSortOrder)
                .orderByAsc(ParkingSpace::getId);
        Page<ParkingSpace> page = spaceMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 车位号模糊搜索 */
    @PostMapping("/search")
    @Operation(summary = "车位号搜索", description = "车位号模糊搜索（按商场ID + 关键词）")
    public Result<List<ParkingSpace>> search(@RequestBody @Parameter(description = "车位号搜索请求", required = true) SpaceSearchRequest request) {
        if (request == null || request.getMallId() == null) {
            throw new BizException(400, "商场ID不能为空");
        }
        String keyword = request.getKeyword() == null ? "" : request.getKeyword();
        int limit = request.getLimit() == null ? 20 : request.getLimit();
        List<ParkingSpace> list = spaceMapper.selectList(Wrappers.lambdaQuery(ParkingSpace.class)
                .eq(ParkingSpace::getMallId, request.getMallId())
                .like(ParkingSpace::getSpaceNo, keyword)
                .orderByAsc(ParkingSpace::getSortOrder)
                .last("LIMIT " + limit));
        return Result.ok(list);
    }

    /** 当前位置附近空闲车位（KNN） */
    @PostMapping("/nearby")
    @Operation(summary = "附近空闲车位", description = "当前位置附近空闲车位（KNN, 入参 mallId/x/y）")
    public Result<List<NearbySpaceVO>> nearby(@RequestBody @Parameter(description = "附近空闲车位查询请求", required = true) NearbyRequest request) {
        if (request == null || request.getMallId() == null || request.getX() == null || request.getY() == null) {
            throw new BizException(400, "mallId/x/y 不能为空");
        }
        int limit = request.getLimit() == null ? 10 : request.getLimit();
        return Result.ok(spaceMapper.selectNearbyFree(request.getMallId(), request.getX(), request.getY(), limit));
    }

    /** 查询车位几何（GeoJSON） */
    @PostMapping("/get-geometry")
    @Operation(summary = "查询车位几何", description = "查询车位几何（GeoJSON, 入参 {\"id\":1}）")
    public Result<SpaceGeometryVO> getGeometry(@RequestBody @Parameter(description = "按ID操作参数", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "车位ID不能为空");
        }
        SpaceGeometryVO vo = spaceMapper.selectGeometry(request.getId());
        if (vo == null) {
            throw new BizException(404, "车位不存在");
        }
        return Result.ok(vo);
    }

    /** 更新车位几何（轮廓 GeoJSON + 入口点 GeoJSON） */
    @PostMapping("/update-geometry")
    @Operation(summary = "更新车位几何", description = "更新车位几何（轮廓 GeoJSON + 入口点 GeoJSON）")
    public Result<Void> updateGeometry(@RequestBody @Parameter(description = "车位几何更新请求", required = true) SpaceGeometryRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "车位ID不能为空");
        }
        int updated = spaceMapper.updateGeometry(request.getId(), request.getGeomGeoJson(), request.getEntranceGeoJson());
        if (updated == 0) {
            throw new BizException(404, "车位不存在");
        }
        return Result.ok();
    }

    /** 手动占用车位（模拟摄像头/地磁/道闸来源） */
    @PostMapping("/occupy")
    @Operation(summary = "手动占用车位", description = "手动占用车位（模拟摄像头/地磁/道闸来源）")
    public Result<Void> occupy(@RequestBody @Parameter(description = "车位占用请求", required = true) OccupyRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "车位ID不能为空");
        }
        String source = request.getSource() == null ? "APP" : request.getSource();
        spaceMapper.updateStatus(request.getId(), "OCCUPIED", source);
        return Result.ok();
    }

    /** 释放车位 */
    @PostMapping("/release")
    @Operation(summary = "释放车位", description = "释放车位（入参 {\"id\":1}）")
    public Result<Void> release(@RequestBody @Parameter(description = "按ID操作参数", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "车位ID不能为空");
        }
        spaceMapper.updateStatus(request.getId(), "FREE", null);
        return Result.ok();
    }
}
