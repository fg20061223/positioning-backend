package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.NavNodeQuery;
import com.positioning.business.dto.NavNodeCreateRequest;
import com.positioning.business.entity.NavNode;
import com.positioning.business.mapper.NavNodeMapper;
import com.positioning.common.api.PageResult;
import com.positioning.common.api.Result;
import com.positioning.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导航节点接口
 */
@RestController
@RequestMapping("/business/nav-node")
@RequiredArgsConstructor
public class NavNodeController extends BaseCrudController<NavNode> {

    private final NavNodeMapper navNodeMapper;

    @Override
    protected BaseMapper<NavNode> baseMapper() {
        return navNodeMapper;
    }

    /** 条件分页: 商场/楼层 */
    @PostMapping("/query")
    public Result<PageResult<NavNode>> query(@RequestBody(required = false) NavNodeQuery request) {
        NavNodeQuery q = request == null ? new NavNodeQuery() : request;
        LambdaQueryWrapper<NavNode> wrapper = Wrappers.lambdaQuery(NavNode.class)
                .eq(q.getMallId() != null, NavNode::getMallId, q.getMallId())
                .eq(q.getFloorId() != null, NavNode::getFloorId, q.getFloorId())
                .orderByAsc(NavNode::getSortOrder)
                .orderByAsc(NavNode::getId);
        Page<NavNode> page = navNodeMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 带几何创建节点（geom 为 NOT NULL 列, 必须走本接口） */
    @PostMapping("/with-geometry")
    public Result<Long> createWithGeometry(@RequestBody NavNodeCreateRequest request) {
        if (request.getGeomGeoJson() == null || request.getGeomGeoJson().isBlank()) {
            throw new BizException(400, "节点几何不能为空");
        }
        Long id = IdWorker.getId();
        int inserted = navNodeMapper.insertWithGeometry(id, request.getMallId(), request.getFloorId(),
                request.getNodeType(), request.getName(), request.getGeomGeoJson(),
                request.getIsAccessible() == null ? Boolean.TRUE : request.getIsAccessible(),
                request.getSortOrder(), request.getRemark());
        if (inserted == 0) {
            throw new BizException(500, "节点创建失败");
        }
        return Result.ok(id);
    }
}
