package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.ShopGeometryVO;
import com.positioning.business.dto.ShopSearchVO;
import com.positioning.business.entity.Shop;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商铺 Mapper（含模糊搜索与空间查询）
 */
public interface ShopMapper extends BaseMapper<Shop> {

    /** 商铺模糊搜索（走 pg_trgm GIN 索引；SQL 见 mapper/ShopMapper.xml） */
    List<ShopSearchVO> search(@Param("mallId") Long mallId,
                              @Param("keyword") String keyword,
                              @Param("limit") int limit);

    /** 查询商铺几何（GeoJSON；SQL 见 mapper/ShopMapper.xml） */
    ShopGeometryVO selectGeometry(@Param("id") Long id);

    /** 更新商铺几何（轮廓+入口点, 中心点自动取质心；SQL 见 mapper/ShopMapper.xml） */
    int updateGeometry(@Param("id") Long id,
                       @Param("geomGeoJson") String geomGeoJson,
                       @Param("entranceGeoJson") String entranceGeoJson);
}
