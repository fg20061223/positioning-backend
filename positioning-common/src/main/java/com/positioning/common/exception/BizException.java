package com.positioning.common.exception;

import lombok.Getter;

/**
 * 业务异常: 由业务代码主动抛出, 由各服务的全局异常处理器统一转为 Result
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务状态码 */
    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
