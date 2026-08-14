package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.OpLog;
import com.positioning.business.mapper.OpLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志接口
 */
@RestController
@RequestMapping("/business/op-log")
@RequiredArgsConstructor
public class OpLogController extends BaseCrudController<OpLog> {

    private final OpLogMapper opLogMapper;

    @Override
    protected BaseMapper<OpLog> baseMapper() {
        return opLogMapper;
    }
}
