package com.positioning.business.dto;

import com.positioning.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商场条件分页查询请求（JSON 入参）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商场条件分页查询请求（JSON 入参）")
public class MallQuery extends PageQuery {

    /** 商场编码（模糊） */
    @Schema(description = "商场编码（模糊）")
    private String mallCode;

    /** 商场名称（模糊） */
    @Schema(description = "商场名称（模糊）")
    private String mallName;

    /** 所在省份 */
    @Schema(description = "所在省份")
    private String province;

    /** 所在城市 */
    @Schema(description = "所在城市")
    private String city;

    /** 所在区县 */
    @Schema(description = "所在区县")
    private String district;

    /** 详细地址（模糊） */
    @Schema(description = "详细地址（模糊）")
    private String address;

    /** 商场状态: 1=营业 0=停用 */
    @Schema(description = "商场状态: 1=营业 0=停用")
    private Integer status;
}
