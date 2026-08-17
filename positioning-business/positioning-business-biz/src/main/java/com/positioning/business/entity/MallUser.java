package com.positioning.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户-商场绑定表: 用户在具体商场内的角色
 */
@Schema(description = "用户-商场绑定表: 用户在具体商场内的角色")
@Data
@TableName("mall_user")
public class MallUser {

    /** 绑定ID（雪花ID，应用层生成） */
    @Schema(description = "绑定ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 商场ID */
    @Schema(description = "商场ID")
    private Long mallId;

    /** 用户ID（跨库引用auth_db） */
    @Schema(description = "用户ID（跨库引用auth_db）")
    private Long userId;

    /** 商场内角色: USER=普通用户 MALL_ADMIN=商场管理员 OPERATOR=运营人员 MERCHANT=商户 */
    @Schema(description = "商场内角色: USER=普通用户 MALL_ADMIN=商场管理员 OPERATOR=运营人员 MERCHANT=商户")
    private String roleCode;

    /** 绑定状态: 1=生效 0=失效 */
    @Schema(description = "绑定状态: 1=生效 0=失效")
    private Integer status;

    /** 创建时间 */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记: 0=正常 1=已删除 */
    @Schema(description = "逻辑删除标记: 0=正常 1=已删除")
    @TableLogic
    private Integer deleted;
}
