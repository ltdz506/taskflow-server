package org.ltdz.taskflow.model.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建任务流时提交的单个任务（CreateTaskflowDto 的子项）。
 */
@Data
public class TaskItemBo {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    @NotBlank(message = "任务要求不能为空")
    private String request;
}
