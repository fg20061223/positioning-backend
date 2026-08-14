package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.MallZone;
import com.positioning.business.mapper.MallZoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分区接口
 */
@RestController
@RequestMapping("/business/zone")
@RequiredArgsConstructor
public class ZoneController extends BaseCrudController<MallZone> {

    private final MallZoneMapper zoneMapper;

    @Override
    protected BaseMapper<MallZone> baseMapper() {
        return zoneMapper;
    }
}
