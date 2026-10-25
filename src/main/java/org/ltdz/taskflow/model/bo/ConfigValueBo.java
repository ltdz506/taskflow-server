package org.ltdz.taskflow.model.bo;

import lombok.Data;
import org.ltdz.taskflow.enums.ConfigControlType;

import java.util.List;

/**
 * 设置项的存储结构，即 configuration.value 里存的 JSON。
 * <p>
 * 示例：{@code {"type":"SELECT","choose":["15","30","60","120"],"value":"30"}}
 */
@Data
public class ConfigValueBo {

    /** 控件类型：TEXT / SELECT / SWITCH */
    private ConfigControlType type;

    /** 下拉框的选项列表；非下拉框类型为空数组 */
    private List<String> choose;

    /** 当前值 */
    private String value;
}
