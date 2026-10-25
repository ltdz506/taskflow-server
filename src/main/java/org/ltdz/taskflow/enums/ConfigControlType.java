package org.ltdz.taskflow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 设置项的控件类型（存入 configuration.value 的 JSON 中）。
 */
@Getter
@RequiredArgsConstructor
public enum ConfigControlType {

    /** 文本框：自由输入 */
    TEXT("文本框"),

    /** 下拉框：从 choose 里选 */
    SELECT("下拉框"),

    /** 开关：二值 */
    SWITCH("开关");

    private final String label;
}
