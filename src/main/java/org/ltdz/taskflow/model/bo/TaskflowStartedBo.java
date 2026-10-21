package org.ltdz.taskflow.model.bo;

import lombok.Data;

/**
 * 事件 taskflow.started 的载荷：任务流被领取。
 */
@Data
public class TaskflowStartedBo {

    private Long taskflowId;

    private String status;

    private String startTime;
}
