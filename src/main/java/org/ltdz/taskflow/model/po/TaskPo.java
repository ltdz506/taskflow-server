package org.ltdz.taskflow.model.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务表实体（task）。
 */
@Data
public class TaskPo {

    /** 任务 id（后端生成） */
    private Long id;

    /** 归属任务流 id */
    private Long taskflowId;

    /** 任务标题 */
    private String title;

    /** 任务要求 */
    private String request;

    /** 任务流内序号（从 1 递增） */
    private Integer order;

    /** 状态：CREATED / WAITING / RUNNING / TERMINATED */
    private String status;

    /** 开始处理时间 */
    private LocalDateTime settleTime;
}
