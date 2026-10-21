package org.ltdz.taskflow.model.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 回复表实体（ai_response），与任务一对一。
 */
@Data
public class AiResponsePo {

    /** 回复 id（后端生成） */
    private Long id;

    /** 关联的任务 id（唯一） */
    private Long taskId;

    /** 回复文本 */
    private String response;

    /** 回复时间 */
    private LocalDateTime respTime;
}
