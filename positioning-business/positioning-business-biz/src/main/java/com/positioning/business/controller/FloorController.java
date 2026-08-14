package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.positioning.business.entity.MallFloor;
import com.positioning.business.mapper.MallFloorMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import com.positioning.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 楼层接口
 */
@RestController
@RequestMapping("/business/floor")
@RequiredArgsConstructor
public class FloorController extends BaseCrudController<MallFloor> {

    private final MallFloorMapper floorMapper;

    @Override
    protected BaseMapper<MallFloor> baseMapper() {
        return floorMapper;
    }

    /** 按商场查询楼层列表（按排序号升序, JSON: {"id":商场ID}） */
    @PostMapping("/by-mall")
    public Result<List<MallFloor>> byMall(@RequestBody IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "商场ID不能为空");
        }
        return Result.ok(floorMapper.selectList(Wrappers.lambdaQuery(MallFloor.class)
                .eq(MallFloor::getMallId, request.getId())
                .orderByAsc(MallFloor::getSortOrder)));
    }
}
