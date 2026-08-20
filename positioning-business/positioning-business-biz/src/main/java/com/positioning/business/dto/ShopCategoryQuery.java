package com.positioning.business.dto;

import com.positioning.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商铺分类条件分页查询请求（JSON 入参）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商铺分类条件分页查询请求（JSON 入参）")
public class ShopCategoryQuery extends PageQuery {

    /** 分类名称（模糊） */
    @Schema(description = "分类名称（模糊）")
    private String catName;

    /** 所属商场ID（空=平台通用分类） */
    @Schema(description = "所属商场ID（空=平台通用分类）")
    private Long mallId;

    /** 父分类ID（0=根分类） */
    @Schema(description = "父分类ID（0=根分类）")
    private Long parentId;
}
