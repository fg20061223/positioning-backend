package com.positioning.business.controller;

import com.positioning.business.dto.DictItemVO;
import com.positioning.business.dto.DictQuery;
import com.positioning.business.entity.SysDict;
import com.positioning.business.mapper.SysDictMapper;
import com.positioning.common.api.Result;
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
 * <p>返回结构: {"dictType": [{"code":"NORMAL","label":"普通","sortOrder":1}, ...], ...}</p>
 */
@Tag(name = "系统字典", description = "字典查询：记录原表结构设计的枚举取值（下拉框/展示文案用）")
@RestController
@RequestMapping("/business/dict")
@RequiredArgsConstructor
public class DictController {

    private final SysDictMapper sysDictMapper;

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
}
