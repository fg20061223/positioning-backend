package com.positioning.business.api.client;

import com.positioning.business.api.dto.SpaceDTO;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 车位 Feign 客户端（预留）
 */
@FeignClient(name = "positioning-business", path = "/internal/space")
public interface SpaceFeignClient {

    /** 按ID查询车位（JSON 入参） */
    @PostMapping("/get")
    Result<SpaceDTO> getSpace(@RequestBody IdRequest request);
}
