package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典项（code + label, 供前端下拉/文案展示）
 */
@Data
@Schema(description = "字典项（code + label）")
public class DictItemVO {

    /** 字典项编码（对应字段存储值） */
    @Schema(description = "字典项编码（对应字段存储值）")
    private String code;

    /** 字典项名称（中文展示文案） */
    @Schema(description = "字典项名称（中文展示文案）")
    private String label;

    /** 排序号（类型内升序） */
    @Schema(description = "排序号（类型内升序）")
    private Integer sortOrder;
}
