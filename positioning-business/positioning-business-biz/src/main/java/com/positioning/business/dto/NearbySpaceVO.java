package com.positioning.business.dto;

import lombok.Data;

/**
 * 附近空闲车位（含距离）
 */
@Data
public class NearbySpaceVO {

    /** 车位ID */
    private Long id;

    /** 车位编号 */
    private String spaceNo;

    /** 车位类型 */
    private String spaceType;

    /** 车位状态 */
    private String status;

    /** 占用来源 */
    private String occupySource;

    /** 所属楼层ID */
    private Long floorId;

    /** 所属分区ID */
    private Long zoneId;

    /** 距定位点距离（米） */
    private Double distanceM;
}
