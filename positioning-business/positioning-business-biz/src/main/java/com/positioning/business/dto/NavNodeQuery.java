package com.positioning.business.dto;

import com.positioning.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导航节点条件分页查询请求（JSON 入参）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NavNodeQuery extends PageQuery {

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;
}
