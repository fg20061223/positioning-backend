package com.positioning.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商铺表
 */
@Data
@TableName("shop")
public class Shop {

    /** 商铺ID（雪花ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属商场ID */
    private Long mallId;

    /** 所属楼层ID */
    private Long floorId;

    /** 所属分区ID */
    private Long zoneId;

    /** 商铺编号，如 1F-101 */
    private String shopNo;

    /** 商铺名称（搜索主字段） */
    private String shopName;

    /** 商铺简称 */
    private String shortName;

    /** 商铺分类ID */
    private Long categoryId;

    /** 品牌名称 */
    private String brand;

    /** 联系电话 */
    private String phone;

    /** 商铺Logo图片地址 */
    private String logoUrl;

    /** 商铺简介 */
    private String description;

    /** 搜索关键词，逗号分隔 */
    private String keywords;

    /** 商铺轮廓（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String geom;

    /** 商铺中心点（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String centerPoint;

    /** 商铺入口点（PostGIS几何, GeoJSON; 通用CRUD不读写, 走专门空间接口） */
    @TableField(select = false, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String entrancePoint;

    /** 商铺状态: OPEN=营业 DECORATING=装修 CLOSED=关闭 */
    private String status;

    /** 排序号 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记: 0=正常 1=已删除 */
    @TableLogic
    private Integer deleted;
}
