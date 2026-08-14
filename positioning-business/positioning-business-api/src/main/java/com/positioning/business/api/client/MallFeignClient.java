package com.positioning.business.api.client;

import com.positioning.business.api.dto.MallDTO;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商场 Feign 客户端（预留: 供认证服务/数据大屏等后续调用）
 * 实现端: business-biz 的 internal 接口
 */
@FeignClient(name = "positioning-business", path = "/internal/mall")
public interface MallFeignClient {

    /** 按ID查询商场（JSON 入参） */
    @PostMapping("/get")
    Result<MallDTO> getMall(@RequestBody IdRequest request);
}
