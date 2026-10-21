package org.ltdz.taskflow.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 回复任务请求体。
 */
@Data
public class RespondToTaskDto {

    /** 要回复的任务 id */
    @NotNull(message = "任务 id 不能为空")
    private Long taskId;

    /** 回复文本 */
    private String response;
}
