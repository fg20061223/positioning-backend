package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车位占用请求（JSON 入参）
 */
@Data
@Schema(description = "车位占用请求（JSON 入参）")
public class OccupyRequest {

    /** 车位ID */
    @Schema(description = "车位ID")
    private Long id;

    /** 占用来源: APP/CAMERA/MAGNET/GATE/MANUAL（默认APP） */
    @Schema(description = "占用来源: APP/CAMERA/MAGNET/GATE/MANUAL（默认APP）")
    private String source;
}
