package com.positioning.business.controller.internal;

import com.positioning.business.api.client.ShopFeignClient;
import com.positioning.business.api.dto.ShopDTO;
import com.positioning.business.entity.Shop;
import com.positioning.business.mapper.ShopMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商铺内部接口（供 Feign 跨服务调用）
 */
@RestController
@RequestMapping("/internal/shop")
@RequiredArgsConstructor
public class InternalShopController implements ShopFeignClient {

    private final ShopMapper shopMapper;

    @Override
    public Result<ShopDTO> getShop(IdRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "商铺ID不能为空");
        }
        Shop shop = shopMapper.selectById(request.getId());
        if (shop == null) {
            return Result.fail(404, "商铺不存在");
        }
        ShopDTO dto = new ShopDTO();
        dto.setId(shop.getId());
        dto.setMallId(shop.getMallId());
        dto.setFloorId(shop.getFloorId());
        dto.setZoneId(shop.getZoneId());
        dto.setShopNo(shop.getShopNo());
        dto.setShopName(shop.getShopName());
        dto.setCategoryId(shop.getCategoryId());
        dto.setStatus(shop.getStatus());
        return Result.ok(dto);
    }
}
