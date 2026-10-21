package org.ltdz.taskflow.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.ltdz.taskflow.model.bo.ConfigItemBo;

import java.util.List;

/**
 * 更新设置值请求体（全量提交）。
 */
@Data
public class UpdateUserConfigDto {

    @NotEmpty(message = "设置项不能为空")
    @Valid
    private List<ConfigItemBo> configurations;
}
