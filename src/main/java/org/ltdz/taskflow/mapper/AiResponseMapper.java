package org.ltdz.taskflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ltdz.taskflow.model.po.AiResponsePo;

import java.util.List;

/**
 * AI 回复表数据访问。
 */
@Mapper
public interface AiResponseMapper {

    int insert(AiResponsePo po);

    AiResponsePo selectByTaskId(@Param("taskId") Long taskId);

    List<AiResponsePo> selectByTaskIds(@Param("taskIds") List<Long> taskIds);

    int deleteByTaskIds(@Param("taskIds") List<Long> taskIds);
}
