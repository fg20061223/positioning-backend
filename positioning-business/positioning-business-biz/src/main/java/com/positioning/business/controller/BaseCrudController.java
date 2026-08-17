package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.common.api.PageResult;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import com.positioning.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通用 CRUD 控制器基类: 分页/详情/新增/修改/删除
 * 统一约定: 全部 POST + JSON 出入参
 */
public abstract class BaseCrudController<E> {

    /** 子类返回对应表的 Mapper */
    protected abstract BaseMapper<E> baseMapper();

    /** 分页查询（JSON: {"pageNum":1,"pageSize":10}） */
    @PostMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询（JSON: {\"pageNum\":1,\"pageSize\":10}）")
    public Result<PageResult<E>> page(@RequestBody(required = false) @Parameter(description = "分页参数（可空）", required = false) PageQuery query) {
        PageQuery q = query == null ? new PageQuery() : query;
        Page<E> page = baseMapper().selectPage(new Page<>(q.getPageNum(), q.getPageSize()), Wrappers.emptyWrapper());
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 按ID查询（JSON: {"id":1}） */
    @PostMapping("/get")
    @Operation(summary = "按ID查询", description = "按ID查询（JSON: {\"id\":1}）")
    public Result<E> get(@RequestBody @Parameter(description = "按ID操作参数", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "ID不能为空");
        }
        E entity = baseMapper().selectById(request.getId());
        if (entity == null) {
            return Result.fail(404, "数据不存在");
        }
        return Result.ok(entity);
    }

    /** 新增 */
    @PostMapping("/create")
    @Operation(summary = "新增", description = "新增一条记录")
    public Result<E> create(@RequestBody @Parameter(description = "新增实体", required = true) E entity) {
        baseMapper().insert(entity);
        return Result.ok(entity);
    }

    /** 修改 */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "按主键修改记录")
    public Result<E> update(@RequestBody @Parameter(description = "待修改实体（含主键）", required = true) E entity) {
        baseMapper().updateById(entity);
        return Result.ok(entity);
    }

    /** 删除（逻辑删除, JSON: {"id":1}） */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "删除（逻辑删除, JSON: {\"id\":1}）")
    public Result<Void> delete(@RequestBody @Parameter(description = "按ID操作参数", required = true) IdRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "ID不能为空");
        }
        baseMapper().deleteById(request.getId());
        return Result.ok();
    }
}
