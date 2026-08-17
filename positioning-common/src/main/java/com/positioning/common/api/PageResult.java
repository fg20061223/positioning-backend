package com.positioning.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一分页结果")
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    @Schema(description = "总记录数")
    private long total;

    /** 当前页数据 */
    @Schema(description = "当前页数据")
    private List<T> records;

    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
