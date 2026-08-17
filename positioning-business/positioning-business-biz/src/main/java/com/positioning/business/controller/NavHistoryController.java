package com.positioning.business.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.entity.NavHistory;
import com.positioning.business.mapper.NavHistoryMapper;
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
 * 导航历史接口
 */
@Tag(name = "导航历史管理", description = "导航历史 CRUD、保存导航记录、我的导航历史")
@RestController
@RequestMapping("/business/nav-history")
@RequiredArgsConstructor
public class NavHistoryController extends BaseCrudController<NavHistory> {

    private final NavHistoryMapper navHistoryMapper;

    @Override
    protected BaseMapper<NavHistory> baseMapper() {
        return navHistoryMapper;
    }

    /** 保存导航记录（自动绑定当前登录用户） */
    @PostMapping("/record")
    @Operation(summary = "保存导航记录", description = "保存导航记录（自动绑定当前登录用户）")
    public Result<NavHistory> record(@RequestBody @Parameter(description = "导航历史实体", required = true) NavHistory history) {
        history.setUserId(StpUtil.getLoginIdAsLong());
        navHistoryMapper.insert(history);
        return Result.ok(history);
    }

    /** 我的导航历史 */
    @PostMapping("/my")
    @Operation(summary = "我的导航历史", description = "我的导航历史（当前登录用户分页）")
    public Result<PageResult<NavHistory>> my(@RequestBody(required = false) @Parameter(description = "分页参数（可空）", required = false) PageQuery query) {
        PageQuery q = query == null ? new PageQuery() : query;
        Long userId = StpUtil.getLoginIdAsLong();
        Page<NavHistory> page = navHistoryMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                Wrappers.lambdaQuery(NavHistory.class)
                        .eq(NavHistory::getUserId, userId)
                        .orderByDesc(NavHistory::getStartTime));
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }
}
