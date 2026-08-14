package com.positioning.business.api.client;

import com.positioning.business.api.dto.ShopDTO;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商铺 Feign 客户端（预留）
 */
@FeignClient(name = "positioning-business", path = "/internal/shop")
public interface ShopFeignClient {

    /** 按ID查询商铺（JSON 入参） */
    @PostMapping("/get")
    Result<ShopDTO> getShop(@RequestBody IdRequest request);
}
