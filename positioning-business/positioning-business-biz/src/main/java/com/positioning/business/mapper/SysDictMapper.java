package com.positioning.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.positioning.business.entity.SysDict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统字典 Mapper
 */
public interface SysDictMapper extends BaseMapper<SysDict> {

    /** 按字典类型列表查询启用项（SQL 见 mapper/SysDictMapper.xml） */
    List<SysDict> selectByTypes(@Param("types") List<String> types);
}
