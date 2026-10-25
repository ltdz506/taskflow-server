package org.ltdz.taskflow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 任务流状态。
 */
@Getter
@RequiredArgsConstructor
public enum TaskflowStatus {

    /** 编辑：用户可修改、可上传附件 */
    EDIT("编辑"),

    /** 启动：被 Agent 领取，执行中 */
    RUNNING("启动"),

    /** 完成：最后一个任务已回复 */
    COMPLETED("完成"),

    /** 销毁：Agent 主动销毁或服务端超时兜底 */
    DESTROYED("销毁");

    private final String label;
}
