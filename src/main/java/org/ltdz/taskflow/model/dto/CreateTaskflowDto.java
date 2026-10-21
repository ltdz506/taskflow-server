package org.ltdz.taskflow.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.ltdz.taskflow.model.bo.TaskItemBo;

import java.util.List;

/**
 * 创建任务流请求体。
 */
@Data
public class CreateTaskflowDto {

    /** 任务流标题（必填） */
    @NotBlank(message = "任务流标题不能为空")
    private String title;

    /** 初始任务要求（必填） */
    @NotBlank(message = "初始任务要求不能为空")
    private String startPrompt;

    /** 后续任务列表（至少 1 个） */
    @NotEmpty(message = "任务流至少需要 1 个任务")
    @Valid
    private List<TaskItemBo> tasks;
}
