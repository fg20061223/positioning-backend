package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.NearbySpaceVO;
import com.positioning.business.dto.SpaceGeometryVO;
import com.positioning.business.entity.ParkingSpace;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 车位 Mapper（含 PostGIS 空间查询）
 */
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {

    /** 当前位置附近 N 个空闲车位（KNN, 走 GIST 索引） */
    @Select("""
            SELECT id, space_no, space_type, status, occupy_source, floor_id, zone_id,
                   ST_Distance(center_point, ST_SetSRID(ST_MakePoint(#{x}, #{y}), 0)) AS distance_m
            FROM parking_space
            WHERE mall_id = #{mallId}
              AND status = 'FREE'
              AND deleted = 0
              AND center_point IS NOT NULL
            ORDER BY center_point <-> ST_SetSRID(ST_MakePoint(#{x}, #{y}), 0)
            LIMIT #{limit}
            """)
    List<NearbySpaceVO> selectNearbyFree(@Param("mallId") Long mallId,
                                         @Param("x") double x,
                                         @Param("y") double y,
                                         @Param("limit") int limit);

    /** 查询车位几何（GeoJSON） */
    @Select("""
            SELECT id,
                   ST_AsGeoJSON(geom) AS geom_geojson,
                   ST_AsGeoJSON(center_point) AS center_geojson,
                   ST_AsGeoJSON(entrance_point) AS entrance_geojson
            FROM parking_space
            WHERE id = #{id}
            """)
    SpaceGeometryVO selectGeometry(@Param("id") Long id);

    /** 更新车位几何（轮廓+入口点, 中心点自动取质心） */
    @Update("""
            UPDATE parking_space
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

    /** 更新车位状态（占用/释放等） */
    @Update("""
            UPDATE parking_space
            SET status = #{status},
                occupy_source = #{source},
                updated_at = now()
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("source") String source);
}
