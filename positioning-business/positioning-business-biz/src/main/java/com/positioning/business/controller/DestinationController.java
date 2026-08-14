package com.positioning.business.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.entity.UserDestination;
import com.positioning.business.mapper.UserDestinationMapper;
import com.positioning.common.api.PageResult;
import com.positioning.common.api.Result;
import com.positioning.common.dto.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收藏接口
 */
@RestController
@RequestMapping("/business/destination")
@RequiredArgsConstructor
public class DestinationController extends BaseCrudController<UserDestination> {

    private final UserDestinationMapper destinationMapper;

    @Override
    protected BaseMapper<UserDestination> baseMapper() {
        return destinationMapper;
    }

    /** 新增收藏（自动绑定当前登录用户） */
    @Override
    @PostMapping("/create")
    public Result<UserDestination> create(@RequestBody UserDestination destination) {
        destination.setUserId(StpUtil.getLoginIdAsLong());
        destinationMapper.insert(destination);
        return Result.ok(destination);
    }

    /** 我的收藏 */
    @PostMapping("/my")
    public Result<PageResult<UserDestination>> my(@RequestBody(required = false) PageQuery query) {
        PageQuery q = query == null ? new PageQuery() : query;
        Long userId = StpUtil.getLoginIdAsLong();
        Page<UserDestination> page = destinationMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                Wrappers.lambdaQuery(UserDestination.class)
                        .eq(UserDestination::getUserId, userId)
                        .orderByAsc(UserDestination::getSortOrder)
                        .orderByDesc(UserDestination::getCreatedAt));
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }
}
