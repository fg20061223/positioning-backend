package com.positioning.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用按ID操作请求（JSON 入参）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用按ID操作请求（JSON 入参）")
public class IdRequest {

    /** 业务主键ID */
    @Schema(description = "业务主键ID")
    private Long id;
}
