package com.positioning.business.dto;

import lombok.Data;

/**
 * 商铺搜索结果
 */
@Data
public class ShopSearchVO {

    /** 商铺ID */
    private Long id;

    /** 商铺编号 */
    private String shopNo;

    /** 商铺名称 */
    private String shopName;

    /** 商铺简称 */
    private String shortName;

    /** 所属楼层ID */
    private Long floorId;

    /** 所属分区ID */
    private Long zoneId;

    /** 商铺分类ID */
    private Long categoryId;

    /** 品牌名称 */
    private String brand;

    /** 联系电话 */
    private String phone;

    /** 商铺状态 */
    private String status;
}
