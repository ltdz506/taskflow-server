package org.ltdz.taskflow.model.vo;

import lombok.Data;

/**
 * 任务流列表项 / Agent 领取的任务流。
 */
@Data
public class TaskflowVo {

    private Long id;

    private String title;

    private String startPrompt;

    /** EDIT / RUNNING / COMPLETED / DESTROYED */
    private String status;

    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createTime;

    /** 启动时间 */
    private String startTime;

    /** 终止时间 */
    private String endTime;
}
