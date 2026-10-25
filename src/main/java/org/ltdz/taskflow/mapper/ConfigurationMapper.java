package org.ltdz.taskflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ltdz.taskflow.model.po.ConfigurationPo;

import java.util.List;

/**
 * 设置项表数据访问。
 */
@Mapper
public interface ConfigurationMapper {

    /**
     * 查某个用户的全部配置项，按 order 升序。
     */
    List<ConfigurationPo> selectByUserId(@Param("userId") Long userId);

    /**
     * 按 id 查单个配置项。
     */
    ConfigurationPo selectById(@Param("id") Long id);

    /**
     * 只更新 value 字段（其余字段不可改）。
     */
    int updateValueById(@Param("id") Long id, @Param("value") String value);
}
