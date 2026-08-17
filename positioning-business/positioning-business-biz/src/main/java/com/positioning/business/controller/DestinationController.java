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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收藏接口
 */
@Tag(name = "用户收藏管理", description = "用户收藏 CRUD、新增收藏（自动绑定登录用户）、我的收藏")
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
    @Operation(summary = "新增收藏", description = "新增收藏（自动绑定当前登录用户）")
    public Result<UserDestination> create(@RequestBody @Parameter(description = "用户收藏实体", required = true) UserDestination destination) {
        destination.setUserId(StpUtil.getLoginIdAsLong());
        destinationMapper.insert(destination);
        return Result.ok(destination);
    }

    /** 我的收藏 */
    @PostMapping("/my")
    @Operation(summary = "我的收藏", description = "我的收藏（当前登录用户分页）")
    public Result<PageResult<UserDestination>> my(@RequestBody(required = false) @Parameter(description = "分页参数（可空）", required = false) PageQuery query) {
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
