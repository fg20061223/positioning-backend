package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.Mall;
import com.positioning.business.mapper.MallMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商场接口
 */
@Tag(name = "商场管理", description = "商场 CRUD（分页/详情/新增/修改/删除）")
@RestController
@RequestMapping("/business/mall")
@RequiredArgsConstructor
public class MallController extends BaseCrudController<Mall> {

    private final MallMapper mallMapper;

    @Override
    protected BaseMapper<Mall> baseMapper() {
        return mallMapper;
    }
}
