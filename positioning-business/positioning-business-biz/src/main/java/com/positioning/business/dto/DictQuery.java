package com.positioning.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 字典查询条件（dictType 单查 / dictTypes 批量 / 均空=返回全部）
 */
@Data
@Schema(description = "字典查询条件（dictType 单查 / dictTypes 批量 / 均空=返回全部）")
public class DictQuery {

    /** 字典类型编码（单个, 如 space_type） */
    @Schema(description = "字典类型编码（单个, 如 space_type）")
    private String dictType;

    /** 字典类型编码列表（批量, 如 [\"space_type\",\"space_status\"]） */
    @Schema(description = "字典类型编码列表（批量, 如 [\"space_type\",\"space_status\"]）")
    private List<String> dictTypes;
}
