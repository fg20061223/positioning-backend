package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.Poi;
import com.positioning.business.mapper.PoiMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础设施/POI 接口
 */
@Tag(name = "基础设施/POI管理", description = "基础设施/POI CRUD（分页/详情/新增/修改/删除）")
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
