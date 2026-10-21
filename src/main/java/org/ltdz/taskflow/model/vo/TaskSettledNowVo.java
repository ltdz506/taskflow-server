package org.ltdz.taskflow.model.vo;

import lombok.Data;

import java.util.List;

/**
 * Agent 取任务结果：轮到执行的任务（可空）+ 其附件 + 是否还有后续任务。
 */
@Data
public class TaskSettledNowVo {

    /** 该任务流已无待执行任务时为 null */
    private TaskVo task;

    /** 该任务的全部附件（输入 + 输出） */
    private List<AssetVo> assets;

    /** 该任务之后是否还有任务 */
    private Boolean hasMore;
}
