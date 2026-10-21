package org.ltdz.taskflow.model.bo;

import lombok.Data;

/**
 * 事件 task.running 的载荷：任务被取用。
 */
@Data
public class TaskRunningBo {

    private Long taskflowId;

    private Long taskId;

    private String status;

    private String settleTime;
}
