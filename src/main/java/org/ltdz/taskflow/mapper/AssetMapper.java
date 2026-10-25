package org.ltdz.taskflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ltdz.taskflow.model.po.AssetPo;

import java.util.List;

/**
 * 附件表数据访问。
 */
@Mapper
public interface AssetMapper {

    int insert(AssetPo po);

    AssetPo selectById(@Param("id") Long id);

    List<AssetPo> selectByTaskId(@Param("taskId") Long taskId);

    List<AssetPo> selectByTaskIds(@Param("taskIds") List<Long> taskIds);

    int countByTaskId(@Param("taskId") Long taskId);

    int deleteByTaskIds(@Param("taskIds") List<Long> taskIds);
}
