package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 下拉选项（仅 id + 名称, 供管理后台/小程序下拉框使用）
 */
@Data
@Schema(description = "下拉选项（仅 id + 名称）")
public class OptionVO {

    /** 选项ID */
    @Schema(description = "选项ID")
    private Long id;

    /** 选项名称 */
    @Schema(description = "选项名称")
    private String name;
}
