package com.positioning.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用按ID操作请求（JSON 入参）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdRequest {

    /** 业务主键ID */
    private Long id;
}
