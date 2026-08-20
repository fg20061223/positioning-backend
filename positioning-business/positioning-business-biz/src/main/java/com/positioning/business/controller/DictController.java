package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.positioning.business.dto.DictItemVO;
import com.positioning.business.dto.DictQuery;
import com.positioning.business.entity.SysDict;
import com.positioning.business.mapper.SysDictMapper;
import com.positioning.common.api.Result;
import com.positioning.common.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统字典接口
 * <p>继承 BaseCrudController 获得 /page|get|create|update|delete 通用接口;
 * create/update 覆写增加"类型+编码唯一"校验; /list 按类型分组返回供下拉。</p>
 */
@Tag(name = "系统字典", description = "字典管理: CRUD(分页/详情/新增/修改/删除) + 按类型分组查询(下拉框用)")
@RestController
@RequestMapping("/business/dict")
@RequiredArgsConstructor
public class DictController extends BaseCrudController<SysDict> {

    private final SysDictMapper sysDictMapper;

    @Override
    protected BaseMapper<SysDict> baseMapper() {
        return sysDictMapper;
    }

    /** 查询字典（dictType 单查 / dictTypes 批量 / 均空=返回全部）, 按类型分组返回 */
    @PostMapping("/list")
    @Operation(summary = "查询字典", description = "查询字典: 入参 {\"dictType\":\"space_type\"} 单查, {\"dictTypes\":[\"space_type\",\"space_status\"]} 批量, 空入参返回全部; 响应按类型分组 {\"space_type\":[{\"code\",\"label\",\"sortOrder\"}]}")
    public Result<Map<String, List<DictItemVO>>> list(@RequestBody(required = false) @Parameter(description = "字典查询条件（可空）", required = false) DictQuery query) {
        List<String> types = new ArrayList<>();
        if (query != null) {
            if (query.getDictType() != null && !query.getDictType().isBlank()) {
                types.add(query.getDictType());
            }
            if (query.getDictTypes() != null) {
                for (String t : query.getDictTypes()) {
                    if (t != null && !t.isBlank() && !types.contains(t)) {
                        types.add(t);
                    }
                }
            }
        }
        List<SysDict> dicts = sysDictMapper.selectByTypes(types);
        Map<String, List<DictItemVO>> result = new LinkedHashMap<>();
        for (SysDict d : dicts) {
            DictItemVO item = new DictItemVO();
            item.setCode(d.getDictCode());
            item.setLabel(d.getDictLabel());
            item.setSortOrder(d.getSortOrder());
            result.computeIfAbsent(d.getDictType(), k -> new ArrayList<>()).add(item);
        }
        return Result.ok(result);
    }

    /** 新增字典（覆写: 校验必填 + 类型内编码唯一） */
    @Override
    @PostMapping("/create")
    @Operation(summary = "新增字典", description = "新增字典项（dictType/dictCode/dictLabel 必填, 同类型下编码不可重复）")
    public Result<SysDict> create(@RequestBody @Parameter(description = "字典实体（dictType/dictCode/dictLabel 必填）", required = true) SysDict entity) {
        validate(entity, null);
        sysDictMapper.insert(entity);
        return Result.ok(entity);
    }

    /** 修改字典（覆写: 校验必填 + 类型内编码唯一, 排除自身） */
    @Override
    @PostMapping("/update")
    @Operation(summary = "修改字典", description = "修改字典项（按 id 更新, dictType/dictCode/dictLabel 必填, 同类型下编码不可重复）")
    public Result<SysDict> update(@RequestBody @Parameter(description = "字典实体（id 必填）", required = true) SysDict entity) {
        if (entity.getId() == null) {
            throw new BizException(400, "字典ID不能为空");
        }
        validate(entity, entity.getId());
        sysDictMapper.updateById(entity);
        return Result.ok(entity);
    }

    /** 校验必填与唯一性（excludeId 用于修改时排除自身） */
    private void validate(SysDict entity, Long excludeId) {
        if (entity.getDictType() == null || entity.getDictType().isBlank()) {
            throw new BizException(400, "字典类型不能为空");
        }
        if (entity.getDictCode() == null || entity.getDictCode().isBlank()) {
            throw new BizException(400, "字典编码不能为空");
        }
        if (entity.getDictLabel() == null || entity.getDictLabel().isBlank()) {
            throw new BizException(400, "字典名称不能为空");
        }
        LambdaQueryWrapper<SysDict> wrapper = Wrappers.lambdaQuery(SysDict.class)
                .eq(SysDict::getDictType, entity.getDictType())
                .eq(SysDict::getDictCode, entity.getDictCode());
        if (excludeId != null) {
            wrapper.ne(SysDict::getId, excludeId);
        }
        Long count = sysDictMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(400, "该字典类型下编码已存在");
        }
    }
}
