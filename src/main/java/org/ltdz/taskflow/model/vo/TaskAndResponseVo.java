package org.ltdz.taskflow.model.vo;

import lombok.Data;

/**
 * 任务流详情中的「任务 + 回复」对。
 * <p>
 * 任务尚未被回复时，response 为 null。
 */
@Data
public class TaskAndResponseVo {

    private TaskVo task;

    private AiResponseVo response;
}
