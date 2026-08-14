package com.positioning.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.positioning.business.dto.ShopGeometryRequest;
import com.positioning.business.dto.ShopGeometryVO;
import com.positioning.business.dto.ShopQuery;
import com.positioning.business.dto.ShopSearchRequest;
import com.positioning.business.dto.ShopSearchVO;
import com.positioning.business.entity.Shop;
import com.positioning.business.mapper.ShopMapper;
import com.positioning.common.api.PageResult;
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
 * 商铺接口（含模糊搜索与空间查询; 全部 POST + JSON）
 */
@RestController
@RequestMapping("/business/shop")
@RequiredArgsConstructor
public class ShopController extends BaseCrudController<Shop> {

    private final ShopMapper shopMapper;

    @Override
    protected BaseMapper<Shop> baseMapper() {
        return shopMapper;
    }

    /** 条件分页: 商场/楼层/分类/状态/关键词 */
    @PostMapping("/query")
    public Result<PageResult<Shop>> query(@RequestBody(required = false) ShopQuery request) {
        ShopQuery q = request == null ? new ShopQuery() : request;
        LambdaQueryWrapper<Shop> wrapper = Wrappers.lambdaQuery(Shop.class)
                .eq(q.getMallId() != null, Shop::getMallId, q.getMallId())
                .eq(q.getFloorId() != null, Shop::getFloorId, q.getFloorId())
                .eq(q.getCategoryId() != null, Shop::getCategoryId, q.getCategoryId())
                .eq(q.getStatus() != null && !q.getStatus().isBlank(), Shop::getStatus, q.getStatus())
                .and(q.getKeyword() != null && !q.getKeyword().isBlank(), w -> w
                        .like(Shop::getShopName, q.getKeyword())
                        .or()
                        .like(Shop::getKeywords, q.getKeyword()))
                .orderByAsc(Shop::getSortOrder)
                .orderByAsc(Shop::getId);
        Page<Shop> page = shopMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 商铺名称模糊搜索（走 pg_trgm 索引） */
    @PostMapping("/search")
    public Result<List<ShopSearchVO>> search(@RequestBody ShopSearchRequest request) {
        if (request == null || request.getMallId() == null) {
            throw new BizException(400, "商场ID不能为空");
        }
        String keyword = request.getKeyword() == null ? "" : request.getKeyword();
        int limit = request.getLimit() == null ? 20 : request.getLimit();
        return Result.ok(shopMapper.search(request.getMallId(), keyword, limit));
    }

    /** 查询商铺几何（GeoJSON） */
    @PostMapping("/get-geometry")
    public Result<ShopGeometryVO> getGeometry(@RequestBody IdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "商铺ID不能为空");
        }
        ShopGeometryVO vo = shopMapper.selectGeometry(request.getId());
        if (vo == null) {
            throw new BizException(404, "商铺不存在");
        }
        return Result.ok(vo);
    }

    /** 更新商铺几何（轮廓 GeoJSON + 入口点 GeoJSON） */
    @PostMapping("/update-geometry")
    public Result<Void> updateGeometry(@RequestBody ShopGeometryRequest request) {
        if (request == null || request.getId() == null) {
            throw new BizException(400, "商铺ID不能为空");
        }
        int updated = shopMapper.updateGeometry(request.getId(), request.getGeomGeoJson(), request.getEntranceGeoJson());
        if (updated == 0) {
            throw new BizException(404, "商铺不存在");
        }
        return Result.ok();
    }
}
