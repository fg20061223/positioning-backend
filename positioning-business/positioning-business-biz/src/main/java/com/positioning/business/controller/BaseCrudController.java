package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.common.api.PageResult;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import com.positioning.common.dto.PageQuery;
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
    public Result<PageResult<E>> page(@RequestBody(required = false) PageQuery query) {
        PageQuery q = query == null ? new PageQuery() : query;
        Page<E> page = baseMapper().selectPage(new Page<>(q.getPageNum(), q.getPageSize()), Wrappers.emptyWrapper());
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 按ID查询（JSON: {"id":1}） */
    @PostMapping("/get")
    public Result<E> get(@RequestBody IdRequest request) {
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
    public Result<E> create(@RequestBody E entity) {
        baseMapper().insert(entity);
        return Result.ok(entity);
    }

    /** 修改 */
    @PostMapping("/update")
    public Result<E> update(@RequestBody E entity) {
        baseMapper().updateById(entity);
        return Result.ok(entity);
    }

    /** 删除（逻辑删除, JSON: {"id":1}） */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody IdRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "ID不能为空");
        }
        baseMapper().deleteById(request.getId());
        return Result.ok();
    }
}
