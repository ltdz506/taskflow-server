package org.ltdz.taskflow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 任务状态（单向流转：创建 → 等待 → 运行 → 终止）。
 */
@Getter
@RequiredArgsConstructor
public enum TaskStatus {

    /** 创建：任务流尚未被领取 */
    CREATED("创建"),

    /** 等待：任务流已启动，排队待执行 */
    WAITING("等待"),

    /** 运行：Agent 已领取该任务 */
    RUNNING("运行"),

    /** 终止：Agent 已回复 */
    TERMINATED("终止");

    private final String label;
}
