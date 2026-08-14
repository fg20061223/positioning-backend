package com.positioning.business.controller.internal;

import com.positioning.business.api.client.SpaceFeignClient;
import com.positioning.business.api.dto.SpaceDTO;
import com.positioning.business.entity.ParkingSpace;
import com.positioning.business.mapper.ParkingSpaceMapper;
import com.positioning.common.api.Result;
import com.positioning.common.dto.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车位内部接口（供 Feign 跨服务调用）
 */
@RestController
@RequestMapping("/internal/space")
@RequiredArgsConstructor
public class InternalSpaceController implements SpaceFeignClient {

    private final ParkingSpaceMapper spaceMapper;

    @Override
    public Result<SpaceDTO> getSpace(IdRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "车位ID不能为空");
        }
        ParkingSpace space = spaceMapper.selectById(request.getId());
        if (space == null) {
            return Result.fail(404, "车位不存在");
        }
        SpaceDTO dto = new SpaceDTO();
        dto.setId(space.getId());
        dto.setMallId(space.getMallId());
        dto.setFloorId(space.getFloorId());
        dto.setZoneId(space.getZoneId());
        dto.setSpaceNo(space.getSpaceNo());
        dto.setSpaceType(space.getSpaceType());
        dto.setStatus(space.getStatus());
        return Result.ok(dto);
    }
}
