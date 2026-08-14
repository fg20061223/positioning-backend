package com.positioning.business.dto;

import lombok.Data;

/**
 * 车位占用请求（JSON 入参）
 */
@Data
public class OccupyRequest {

    /** 车位ID */
    private Long id;

    /** 占用来源: APP/CAMERA/MAGNET/GATE/MANUAL（默认APP） */
    private String source;
}
