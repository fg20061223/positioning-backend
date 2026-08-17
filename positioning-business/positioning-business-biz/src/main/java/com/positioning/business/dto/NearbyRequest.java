package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 附近空闲车位查询请求（JSON 入参）
 */
@Data
@Schema(description = "附近空闲车位查询请求（JSON 入参）")
public class NearbyRequest {

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 定位点X坐标（楼层本地米制坐标） */
    @Schema(description = "定位点X坐标（楼层本地米制坐标）")
    private Double x;

    /** 定位点Y坐标（楼层本地米制坐标） */
    @Schema(description = "定位点Y坐标（楼层本地米制坐标）")
    private Double y;

    /** 返回条数上限（默认10） */
    @Schema(description = "返回条数上限（默认10）")
    private Integer limit;
}
