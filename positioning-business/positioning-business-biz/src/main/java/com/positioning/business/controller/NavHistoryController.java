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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导航历史接口
 */
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
    public Result<NavHistory> record(@RequestBody NavHistory history) {
        history.setUserId(StpUtil.getLoginIdAsLong());
        navHistoryMapper.insert(history);
        return Result.ok(history);
    }

    /** 我的导航历史 */
    @PostMapping("/my")
    public Result<PageResult<NavHistory>> my(@RequestBody(required = false) PageQuery query) {
        PageQuery q = query == null ? new PageQuery() : query;
        Long userId = StpUtil.getLoginIdAsLong();
        Page<NavHistory> page = navHistoryMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                Wrappers.lambdaQuery(NavHistory.class)
                        .eq(NavHistory::getUserId, userId)
                        .orderByDesc(NavHistory::getStartTime));
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }
}
