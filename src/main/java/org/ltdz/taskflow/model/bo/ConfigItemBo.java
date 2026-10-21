package org.ltdz.taskflow.model.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置项的「id - 值」对（UpdateUserConfigDto 的子项）。
 */
@Data
public class ConfigItemBo {

    @NotNull(message = "设置项 id 不能为空")
    private Long id;

    @NotNull(message = "设置值不能为空")
    private String value;
}
