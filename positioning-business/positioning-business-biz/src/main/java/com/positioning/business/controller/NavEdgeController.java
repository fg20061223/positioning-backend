package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.NavEdgeQuery;
import com.positioning.business.entity.NavEdge;
import com.positioning.business.mapper.NavEdgeMapper;
import com.positioning.common.api.PageResult;
import com.positioning.common.api.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导航边接口
 */
@RestController
@RequestMapping("/business/nav-edge")
@RequiredArgsConstructor
public class NavEdgeController extends BaseCrudController<NavEdge> {

    private final NavEdgeMapper navEdgeMapper;

    @Override
    protected BaseMapper<NavEdge> baseMapper() {
        return navEdgeMapper;
    }

    /** 按商场分页查询 */
    @PostMapping("/query")
    public Result<PageResult<NavEdge>> query(@RequestBody(required = false) NavEdgeQuery request) {
        NavEdgeQuery q = request == null ? new NavEdgeQuery() : request;
        LambdaQueryWrapper<NavEdge> wrapper = Wrappers.lambdaQuery(NavEdge.class)
                .eq(q.getMallId() != null, NavEdge::getMallId, q.getMallId())
                .orderByAsc(NavEdge::getFromNodeId)
                .orderByAsc(NavEdge::getToNodeId);
        Page<NavEdge> page = navEdgeMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }
}
