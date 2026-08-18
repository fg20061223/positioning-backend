package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.NavNode;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 导航节点 Mapper
 */
public interface NavNodeMapper extends BaseMapper<NavNode> {

    /** 查询某商场所有可通行节点 */
    @Select("""
            SELECT id, mall_id, floor_id, node_type, name, is_accessible, sort_order, remark
            FROM nav_node
            WHERE mall_id = #{mallId}
              AND deleted = 0
              AND is_accessible = TRUE
            """)
    List<NavNode> selectAccessibleByMall(@Param("mallId") Long mallId);

    /** 带几何创建节点（geom 为 NOT NULL 列, 必须走本接口写入） */
    @Insert("""
            INSERT INTO nav_node (id, mall_id, floor_id, node_type, name, geom, is_accessible, sort_order, remark, created_at, updated_at)
            VALUES (#{id}, #{mallId}, #{floorId}, #{nodeType}, #{name},
                    ST_SetSRID(ST_GeomFromGeoJSON(#{geomGeoJson}), 0),
                    #{isAccessible}, #{sortOrder}, #{remark}, now(), now())
            """)
    int insertWithGeometry(@Param("id") Long id,
                           @Param("mallId") Long mallId,
                           @Param("floorId") Long floorId,
                           @Param("nodeType") String nodeType,
                           @Param("name") String name,
                           @Param("geomGeoJson") String geomGeoJson,
                           @Param("isAccessible") Boolean isAccessible,
                           @Param("sortOrder") Integer sortOrder,
                           @Param("remark") String remark);

    /** 更新节点几何（编辑器拖拽移动节点; geom 为 NOT NULL 列, 走本方法绕过通用 CRUD） */
    @Update("""
            UPDATE nav_node
            SET geom = ST_SetSRID(ST_GeomFromGeoJSON(#{geomGeoJson}), 0),
                updated_at = now()
            WHERE id = #{id}
              AND deleted = 0
            """)
    int updateGeometry(@Param("id") Long id, @Param("geomGeoJson") String geomGeoJson);
}
