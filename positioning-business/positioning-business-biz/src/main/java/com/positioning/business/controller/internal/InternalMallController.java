package com.positioning.business.controller.internal;

import com.positioning.business.api.client.MallFeignClient;
import com.positioning.business.api.dto.MallDTO;
import com.positioning.business.entity.Mall;
import com.positioning.business.mapper.MallMapper;
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
 * 商场内部接口（供 Feign 跨服务调用）
 */
@Tag(name = "商场内部接口", description = "商场内部接口（供 Feign 跨服务调用）")
@RestController
@RequestMapping("/internal/mall")
@RequiredArgsConstructor
public class InternalMallController implements MallFeignClient {

    private final MallMapper mallMapper;

    @Override
    @Operation(summary = "查询商场", description = "按ID查询商场（供 Feign 跨服务调用, 入参 {\"id\":1}）")
    public Result<MallDTO> getMall(@RequestBody @Parameter(description = "按ID操作参数", required = true) IdRequest request) {
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
