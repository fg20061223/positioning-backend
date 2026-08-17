package com.positioning.business.controller;

import com.positioning.business.dto.RouteRequest;
import com.positioning.business.dto.RouteVO;
import com.positioning.business.service.NavService;
import com.positioning.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 路径规划接口
 */
@Tag(name = "路径规划管理", description = "路径规划（Dijkstra）")
@RestController
@RequestMapping("/business/nav")
@RequiredArgsConstructor
public class RouteController {

    private final NavService navService;

    /** 路径规划（Dijkstra） */
    @PostMapping("/route")
    @Operation(summary = "路径规划", description = "路径规划（Dijkstra）, 入参 商场ID + 起点节点ID + 终点节点ID")
    public Result<RouteVO> route(@RequestBody @Valid @Parameter(description = "路径规划请求", required = true) RouteRequest request) {
        return Result.ok(navService.route(request.getMallId(), request.getFromNodeId(), request.getToNodeId()));
    }
}
