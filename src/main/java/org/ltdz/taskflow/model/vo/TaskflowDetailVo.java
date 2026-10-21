package org.ltdz.taskflow.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 任务流详情（对话形式）。
 */
@Data
public class TaskflowDetailVo {

    private Long taskflowId;

    /** 初始任务要求（无 AI 回复） */
    private String startPrompt;

    private List<TaskAndResponseVo> taskAndResponses;
}
