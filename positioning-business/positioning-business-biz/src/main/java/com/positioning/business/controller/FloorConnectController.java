package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.FloorConnect;
import com.positioning.business.mapper.FloorConnectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跨楼层连接接口
 */
@Tag(name = "跨楼层连接管理", description = "跨楼层连接 CRUD（分页/详情/新增/修改/删除）")
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
