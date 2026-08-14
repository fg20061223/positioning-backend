package com.positioning.business.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商场 DTO（Feign 跨服务传输）
 */
@Data
@NoArgsConstructor
public class MallDTO implements Serializable {

    /** 商场ID */
    private Long id;

    /** 商场编码 */
    private String mallCode;

    /** 商场名称 */
    private String mallName;

    /** 所在城市 */
    private String city;

    /** 详细地址 */
    private String address;

    /** 商场状态: 1=营业 0=停用 */
    private Integer status;
}
