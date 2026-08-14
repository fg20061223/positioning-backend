package com.positioning.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表: 记录运营人员对空间/车位/商铺数据的操作
 */
@Data
@TableName("op_log")
public class OpLog {

    /** 日志ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 操作人用户ID */
    private Long userId;

    /** 所属商场ID */
    private Long mallId;

    /** 操作模块（如车位、商铺、导航图） */
    private String module;

    /** 操作动作（如新增、修改、删除） */
    private String action;

    /** 操作对象类型 */
    private String targetType;

    /** 操作对象ID */
    private Long targetId;

    /** 操作详情（JSON格式） */
    private String detail;

    /** 操作人IP地址 */
    private String ip;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
