package com.positioning.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果
 */
@Data
@NoArgsConstructor
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    /** 业务状态码: 200=成功, 其余为业务/系统错误 */
    @Schema(description = "状态码 200=成功")
    private int code;

    /** 提示信息 */
    @Schema(description = "提示信息")
    private String message;

    /** 响应数据 */
    @Schema(description = "数据")
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功 */
    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }

    /** 成功并携带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 失败（默认 500） */
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    /** 失败（自定义状态码） */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
