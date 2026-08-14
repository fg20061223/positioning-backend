package com.positioning.business.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 车位 DTO（Feign 跨服务传输）
 */
@Data
@NoArgsConstructor
public class SpaceDTO implements Serializable {

    /** 车位ID */
    private Long id;

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;

    /** 所属分区ID */
    private Long zoneId;

    /** 车位编号，如 B3-012 */
    private String spaceNo;

    /** 车位类型 */
    private String spaceType;

    /** 车位状态: FREE/OCCUPIED/LOCKED/FAULT */
    private String status;
}
