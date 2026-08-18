package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.ShopGeometryVO;
import com.positioning.business.dto.ShopSearchVO;
import com.positioning.business.entity.Shop;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商铺 Mapper（含模糊搜索与空间查询）
 */
public interface ShopMapper extends BaseMapper<Shop> {

    /** 商铺模糊搜索（走 pg_trgm GIN 索引） */
    @Select("""
            SELECT id, shop_no, shop_name, short_name, floor_id, zone_id, category_id, brand, phone, status
            FROM shop
            WHERE mall_id = #{mallId}
              AND deleted = 0
              AND (shop_name ILIKE CONCAT('%', #{keyword}, '%')
                   OR keywords ILIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY sort_order, id
            LIMIT #{limit}
            """)
    List<ShopSearchVO> search(@Param("mallId") Long mallId,
                              @Param("keyword") String keyword,
                              @Param("limit") int limit);

    /** 查询商铺几何（GeoJSON） */
    @Select("""
            SELECT id,
                   ST_AsGeoJSON(geom) AS geom_geojson,
                   ST_AsGeoJSON(center_point) AS center_geojson,
                   ST_AsGeoJSON(entrance_point) AS entrance_geojson
            FROM shop
            WHERE id = #{id}
            """)
    ShopGeometryVO selectGeometry(@Param("id") Long id);

    /** 更新商铺几何（轮廓+入口点, 中心点自动取质心） */
    @Update("""
            UPDATE shop
            SET geom = ST_SetSRID(ST_GeomFromGeoJSON(#{geomGeoJson}), 0),
                center_point = ST_Centroid(ST_SetSRID(ST_GeomFromGeoJSON(#{geomGeoJson}), 0)),
                entrance_point = CASE
                    WHEN #{entranceGeoJson} IS NOT NULL AND #{entranceGeoJson} <> ''
                    THEN ST_SetSRID(ST_GeomFromGeoJSON(#{entranceGeoJson}), 0)
                    ELSE NULL END,
                updated_at = now()
            WHERE id = #{id}
            """)
    int updateGeometry(@Param("id") Long id,
                       @Param("geomGeoJson") String geomGeoJson,
                       @Param("entranceGeoJson") String entranceGeoJson);
}
