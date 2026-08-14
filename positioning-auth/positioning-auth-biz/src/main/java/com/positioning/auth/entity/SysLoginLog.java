package com.positioning.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志表: 记录每次登录成功/失败
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    /** 日志ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID（未登录成功时为空） */
    private Long userId;

    /** 登录用户名 */
    private String username;

    /** 登录IP地址 */
    private String ip;

    /** 浏览器/客户端User-Agent */
    private String userAgent;

    /** 登录结果: 1=成功 0=失败 */
    private Integer success;

    /** 登录失败原因 */
    private String failReason;

    /** 登录时间 */
    private LocalDateTime loginAt;
}
