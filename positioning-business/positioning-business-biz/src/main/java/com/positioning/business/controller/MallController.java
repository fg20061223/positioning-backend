package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.Mall;
import com.positioning.business.mapper.MallMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商场接口
 */
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
