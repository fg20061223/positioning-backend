package com.positioning.business.controller.internal;

import com.positioning.business.api.client.MallFeignClient;
import com.positioning.business.api.dto.MallDTO;
import com.positioning.business.entity.Mall;
import com.positioning.business.mapper.MallMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商场内部接口（供 Feign 跨服务调用）
 */
@RestController
@RequestMapping("/internal/mall")
@RequiredArgsConstructor
public class InternalMallController implements MallFeignClient {

    private final MallMapper mallMapper;

    @Override
    public Result<MallDTO> getMall(IdRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "商场ID不能为空");
        }
        Mall mall = mallMapper.selectById(request.getId());
        if (mall == null) {
            return Result.fail(404, "商场不存在");
        }
        MallDTO dto = new MallDTO();
        dto.setId(mall.getId());
        dto.setMallCode(mall.getMallCode());
        dto.setMallName(mall.getMallName());
        dto.setCity(mall.getCity());
        dto.setAddress(mall.getAddress());
        dto.setStatus(mall.getStatus());
        return Result.ok(dto);
    }
}
