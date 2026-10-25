package org.ltdz.taskflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ltdz.taskflow.model.po.TaskPo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务表数据访问。
 */
@Mapper
public interface TaskMapper {

    int insertBatch(@Param("tasks") List<TaskPo> tasks);

    List<TaskPo> selectByTaskflowId(@Param("taskflowId") Long taskflowId);

    TaskPo selectById(@Param("id") Long id);

    /**
     * 取某任务流中指定状态下 order 最小的任务。
     */
    TaskPo selectFirstByStatus(@Param("taskflowId") Long taskflowId, @Param("status") String status);

    /**
     * 取某任务流中 order 最大的任务（判断是否为最后一条）。
     */
    TaskPo selectLastByTaskflowId(@Param("taskflowId") Long taskflowId);

    int countByTaskflowId(@Param("taskflowId") Long taskflowId);

    List<Long> selectIdsByTaskflowId(@Param("taskflowId") Long taskflowId);

    /**
     * 任务流启动：把该任务流下某状态的任务批量改为另一状态。
     */
    int updateStatusByTaskflowId(@Param("taskflowId") Long taskflowId,
                                 @Param("fromStatus") String fromStatus,
                                 @Param("toStatus") String toStatus);

    /**
     * 取任务：改状态 + 写 settleTime。
     */
    int updateStatusAndSettleTime(@Param("id") Long id,
                                  @Param("status") String status,
                                  @Param("settleTime") LocalDateTime settleTime);

    int deleteByTaskflowId(@Param("taskflowId") Long taskflowId);
}
