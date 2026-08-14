package com.positioning.business.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商铺 DTO（Feign 跨服务传输）
 */
@Data
@NoArgsConstructor
public class ShopDTO implements Serializable {

    /** 商铺ID */
    private Long id;

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;

    /** 所属分区ID */
    private Long zoneId;

    /** 商铺编号 */
    private String shopNo;

    /** 商铺名称 */
    private String shopName;

    /** 商铺分类ID */
    private Long categoryId;

    /** 商铺状态: OPEN/DECORATING/CLOSED */
    private String status;
}
