package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.FloorConnect;
import com.positioning.business.mapper.FloorConnectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跨楼层连接接口
 */
@RestController
@RequestMapping("/business/floor-connect")
@RequiredArgsConstructor
public class FloorConnectController extends BaseCrudController<FloorConnect> {

    private final FloorConnectMapper floorConnectMapper;

    @Override
    protected BaseMapper<FloorConnect> baseMapper() {
        return floorConnectMapper;
    }
}
