package org.ltdz.taskflow.model.bo;

import lombok.Data;

/**
 * 事件 taskflow.completed 的载荷：最后一个任务回复完，任务流完成。
 */
@Data
public class TaskflowCompletedBo {

    private Long taskflowId;

    private String status;

    private String endTime;
}
