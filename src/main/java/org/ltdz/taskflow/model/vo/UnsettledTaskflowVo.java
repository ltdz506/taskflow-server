package org.ltdz.taskflow.model.vo;

import lombok.Data;

/**
 * Agent 领取结果：一个待处理任务流（可空）+ 是否还有更多。
 */
@Data
public class UnsettledTaskflowVo {

    /** 没有待处理任务流时为 null */
    private TaskflowVo taskflow;

    /** 除此任务流外，是否还有待处理的任务流 */
    private Boolean hasMore;
}
