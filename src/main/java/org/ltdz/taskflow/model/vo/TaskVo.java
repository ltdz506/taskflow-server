package org.ltdz.taskflow.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 任务信息（详情、Agent 取任务共用）。
 */
@Data
public class TaskVo {

    private Long id;

    private Long taskflowId;

    private String title;

    private String request;

    /** 任务流内序号 */
    private Integer order;

    /** CREATED / WAITING / RUNNING / TERMINATED */
    private String status;

    /** 开始处理时间（yyyy-MM-dd HH:mm:ss） */
    private String settleTime;

    /** 该任务的输入附件（用户上传，isInput = 1） */
    private List<AssetVo> assets;
}
