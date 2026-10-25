package org.ltdz.taskflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ltdz.taskflow.model.po.TaskflowPo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务流表数据访问。
 */
@Mapper
public interface TaskflowMapper {

    int insert(TaskflowPo po);

    TaskflowPo selectById(@Param("id") Long id);

    /**
     * 分页查（创建时间降序），keyword 可选（标题模糊）。
     */
    List<TaskflowPo> selectPage(@Param("userId") Long userId,
                                @Param("keyword") String keyword,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    long countByUser(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 取某用户指定状态下最早创建的任务流。
     */
    TaskflowPo selectEarliestByStatus(@Param("userId") Long userId, @Param("status") String status);

    long countByStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 全局按状态查（定时任务用，不限用户）。
     */
    List<TaskflowPo> selectByStatus(@Param("status") String status);

    /**
     * 查已结束的任务流（完成 / 销毁，且有 endTime）。
     */
    List<TaskflowPo> selectFinished();

    /**
     * 领取：改状态 + 写 startTime。
     */
    int updateStatusAndStartTime(@Param("id") Long id,
                                 @Param("status") String status,
                                 @Param("startTime") LocalDateTime startTime);

    /**
     * 完成 / 销毁：改状态 + 写 endTime。
     */
    int updateStatusAndEndTime(@Param("id") Long id,
                               @Param("status") String status,
                               @Param("endTime") LocalDateTime endTime);

    int deleteById(@Param("id") Long id);
}
