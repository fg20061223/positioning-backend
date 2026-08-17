package com.positioning.business.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 车位 DTO（Feign 跨服务传输）
 */
@Data
@NoArgsConstructor
@Schema(description = "车位 DTO（Feign 跨服务传输）")
public class SpaceDTO implements Serializable {

    /** 车位ID */
    @Schema(description = "车位ID")
    private Long id;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 所属楼层ID */
    @Schema(description = "所属楼层ID")
    private Long floorId;

    /** 所属分区ID */
    @Schema(description = "所属分区ID")
    private Long zoneId;

    /** 车位编号，如 B3-012 */
    @Schema(description = "车位编号，如 B3-012")
    private String spaceNo;

    /** 车位类型 */
    @Schema(description = "车位类型")
    private String spaceType;

    /** 车位状态: FREE/OCCUPIED/LOCKED/FAULT */
    @Schema(description = "车位状态: FREE/OCCUPIED/LOCKED/FAULT")
    private String status;
}
