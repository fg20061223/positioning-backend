package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.UserVehicle;
import com.positioning.business.mapper.UserVehicleMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户车辆接口
 */
@Tag(name = "用户车辆管理", description = "用户车辆 CRUD（分页/详情/新增/修改/删除）")
@RestController
@RequestMapping("/business/vehicle")
@RequiredArgsConstructor
public class VehicleController extends BaseCrudController<UserVehicle> {

    private final UserVehicleMapper vehicleMapper;

    @Override
    protected BaseMapper<UserVehicle> baseMapper() {
        return vehicleMapper;
    }
}
