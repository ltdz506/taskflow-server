package org.ltdz.taskflow.model.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务流表实体（taskflow）。
 */
@Data
public class TaskflowPo {

    /** 任务流 id（后端生成） */
    private Long id;

    /** 归属用户 id */
    private Long userId;

    /** 任务流标题 */
    private String title;

    /** 初始任务要求 */
    private String startPrompt;

    /** 状态：EDIT / RUNNING / COMPLETED / DESTROYED */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 启动时间 */
    private LocalDateTime startTime;

    /** 终止时间 */
    private LocalDateTime endTime;
}
