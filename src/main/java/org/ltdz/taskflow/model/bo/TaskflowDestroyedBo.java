package org.ltdz.taskflow.model.bo;

import lombok.Data;

/**
 * 事件 taskflow.destroyed 的载荷：任务流被销毁。
 */
@Data
public class TaskflowDestroyedBo {

    private Long taskflowId;

    private String status;

    private String endTime;
}
