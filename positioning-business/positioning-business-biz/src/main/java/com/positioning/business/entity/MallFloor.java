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
 * 商场楼层表
 */
@Schema(description = "商场楼层表")
@Data
@TableName("mall_floor")
public class MallFloor {

    /** 楼层ID（雪花ID，应用层生成） */
    @Schema(description = "楼层ID（雪花ID，应用层生成）")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    @Schema(description = "所属商场ID")
    private Long mallId;

    /** 楼层编码，如 B3/B2/B1/1F/2F */
    @Schema(description = "楼层编码，如 B3/B2/B1/1F/2F")
    private String floorCode;

    /** 楼层名称 */
    @Schema(description = "楼层名称")
    private String floorName;

    /** 楼层排序号（地下到地上递增） */
    @Schema(description = "楼层排序号（地下到地上递增）")
    private Integer sortOrder;

    /** 楼层状态: 1=开放 0=关闭 */
    @Schema(description = "楼层状态: 1=开放 0=关闭")
    private Integer status;

    /** 楼层平面图宽度（米） */
    @Schema(description = "楼层平面图宽度（米）")
    private Double widthM;

    /** 楼层平面图高度（米） */
    @Schema(description = "楼层平面图高度（米）")
    private Double heightM;

    /** 楼层平面图URL（上传后返回, 导航图编辑器底图） */
    @Schema(description = "楼层平面图URL（上传后返回, 导航图编辑器底图）")
    private String imageUrl;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

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
