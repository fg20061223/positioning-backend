package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.dto.NavNodeGeoVO;
import com.positioning.business.entity.NavNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导航节点 Mapper
 */
public interface NavNodeMapper extends BaseMapper<NavNode> {

    /** 查询某商场所有可通行节点（SQL 见 mapper/NavNodeMapper.xml） */
    List<NavNode> selectAccessibleByMall(@Param("mallId") Long mallId);

    /** 带几何创建节点（geom 为 NOT NULL 列, 必须走本接口写入；SQL 见 mapper/NavNodeMapper.xml） */
    int insertWithGeometry(@Param("id") Long id,
                           @Param("mallId") Long mallId,
                           @Param("floorId") Long floorId,
                           @Param("nodeType") String nodeType,
                           @Param("name") String name,
                           @Param("geomGeoJson") String geomGeoJson,
                           @Param("isAccessible") Boolean isAccessible,
                           @Param("sortOrder") Integer sortOrder,
                           @Param("remark") String remark);

    /** 更新节点几何（编辑器拖拽移动节点; geom 为 NOT NULL 列, 走本方法绕过通用 CRUD；SQL 见 mapper/NavNodeMapper.xml） */
    int updateGeometry(@Param("id") Long id, @Param("geomGeoJson") String geomGeoJson);

    /** 按商场/楼层查询节点几何（GeoJSON, 供导航图编辑器加载；SQL 见 mapper/NavNodeMapper.xml） */
    List<NavNodeGeoVO> selectGeoByFloor(@Param("mallId") Long mallId, @Param("floorId") Long floorId);
}
