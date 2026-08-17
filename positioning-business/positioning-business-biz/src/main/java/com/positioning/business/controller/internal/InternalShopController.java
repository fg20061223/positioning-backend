package com.positioning.business.controller.internal;

import com.positioning.business.api.client.ShopFeignClient;
import com.positioning.business.api.dto.ShopDTO;
import com.positioning.business.entity.Shop;
import com.positioning.business.mapper.ShopMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商铺内部接口（供 Feign 跨服务调用）
 */
@Tag(name = "商铺内部接口", description = "商铺内部接口（供 Feign 跨服务调用）")
@RestController
@RequestMapping("/internal/shop")
@RequiredArgsConstructor
public class InternalShopController implements ShopFeignClient {

    private final ShopMapper shopMapper;

    @Override
    @Operation(summary = "查询商铺", description = "按ID查询商铺（供 Feign 跨服务调用, 入参 {\"id\":1}）")
    public Result<ShopDTO> getShop(@RequestBody @Parameter(description = "按ID操作参数", required = true) IdRequest request) {
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
