package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.NearbySpaceVO;
import com.positioning.business.dto.SpaceGeometryVO;
import com.positioning.business.entity.ParkingSpace;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车位 Mapper（含 PostGIS 空间查询）
 */
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {

    /** 当前位置附近 N 个空闲车位（KNN, 走 GIST 索引；SQL 见 mapper/ParkingSpaceMapper.xml） */
    List<NearbySpaceVO> selectNearbyFree(@Param("mallId") Long mallId,
                                         @Param("x") double x,
                                         @Param("y") double y,
                                         @Param("limit") int limit);

    /** 查询车位几何（GeoJSON；SQL 见 mapper/ParkingSpaceMapper.xml） */
    SpaceGeometryVO selectGeometry(@Param("id") Long id);

    /** 更新车位几何（轮廓+入口点, 中心点自动取质心；SQL 见 mapper/ParkingSpaceMapper.xml） */
    int updateGeometry(@Param("id") Long id,
                       @Param("geomGeoJson") String geomGeoJson,
                       @Param("entranceGeoJson") String entranceGeoJson);

    /** 更新车位状态（占用/释放等；SQL 见 mapper/ParkingSpaceMapper.xml） */
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("source") String source);
}
