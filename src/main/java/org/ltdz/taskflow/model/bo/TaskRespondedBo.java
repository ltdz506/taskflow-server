package org.ltdz.taskflow.model.bo;

import lombok.Data;

/**
 * 事件 task.responded 的载荷：任务被回复并产生 AI 回复。
 */
@Data
public class TaskRespondedBo {

    private Long taskflowId;

    private Long taskId;

    private String status;

    private String response;

    private String respTime;
}
