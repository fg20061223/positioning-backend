package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.Poi;
import com.positioning.business.mapper.PoiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础设施/POI 接口
 */
@RestController
@RequestMapping("/business/poi")
@RequiredArgsConstructor
public class PoiController extends BaseCrudController<Poi> {

    private final PoiMapper poiMapper;

    @Override
    protected BaseMapper<Poi> baseMapper() {
        return poiMapper;
    }
}
