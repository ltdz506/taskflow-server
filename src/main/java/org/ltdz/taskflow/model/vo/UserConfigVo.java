package org.ltdz.taskflow.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 设置页返回：用户 id + 全部设置项。
 */
@Data
public class UserConfigVo {

    private Long userId;

    private List<ConfigurationVo> configurations;
}
