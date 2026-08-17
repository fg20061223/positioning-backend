package com.positioning.business.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.positioning.business.entity.PositioningSession;
import com.positioning.business.mapper.PositioningSessionMapper;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定位会话接口
 */
@Tag(name = "定位会话管理", description = "定位会话 CRUD、开始/结束会话、我的进行中会话")
@RestController
@RequestMapping("/business/positioning-session")
@RequiredArgsConstructor
public class PositioningSessionController extends BaseCrudController<PositioningSession> {

    private final PositioningSessionMapper sessionMapper;

    @Override
    protected BaseMapper<PositioningSession> baseMapper() {
        return sessionMapper;
    }

    /** 开始定位会话 */
    @PostMapping("/start")
    @Operation(summary = "开始定位会话", description = "开始定位会话（自动绑定当前登录用户）")
    public Result<PositioningSession> start(@RequestBody @Parameter(description = "定位会话实体", required = true) PositioningSession session) {
        session.setUserId(StpUtil.getLoginIdAsLong());
        session.setStartTime(LocalDateTime.now());
        session.setStatus("ACTIVE");
        sessionMapper.insert(session);
        return Result.ok(session);
    }

    /** 结束定位会话（JSON: {"id":会话ID}） */
    @PostMapping("/end")
    @Operation(summary = "结束定位会话", description = "结束定位会话（入参 {\"id\":会话ID}）")
    public Result<PositioningSession> end(@RequestBody @Parameter(description = "按ID操作参数（会话ID）", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "会话ID不能为空");
        }
        PositioningSession session = sessionMapper.selectById(request.getId());
        if (session == null) {
            throw new BizException(404, "定位会话不存在");
        }
        session.setEndTime(LocalDateTime.now());
        session.setStatus("ENDED");
        sessionMapper.updateById(session);
        return Result.ok(session);
    }

    /** 我的进行中会话 */
    @PostMapping("/active")
    @Operation(summary = "我的进行中会话", description = "我的进行中会话（当前登录用户 ACTIVE 会话列表）")
    public Result<List<PositioningSession>> active() {
        return Result.ok(sessionMapper.selectList(Wrappers.lambdaQuery(PositioningSession.class)
                .eq(PositioningSession::getUserId, StpUtil.getLoginIdAsLong())
                .eq(PositioningSession::getStatus, "ACTIVE")));
    }
}
