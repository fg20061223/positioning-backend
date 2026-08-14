package com.positioning.business.controller;

import com.positioning.business.dto.RouteRequest;
import com.positioning.business.dto.RouteVO;
import com.positioning.business.service.NavService;
import com.positioning.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 路径规划接口
 */
@RestController
@RequestMapping("/business/nav")
@RequiredArgsConstructor
public class RouteController {

    private final NavService navService;

    /** 路径规划（Dijkstra） */
    @PostMapping("/route")
    public Result<RouteVO> route(@RequestBody @Valid RouteRequest request) {
        return Result.ok(navService.route(request.getMallId(), request.getFromNodeId(), request.getToNodeId()));
    }
}
