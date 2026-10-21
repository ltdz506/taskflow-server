package org.ltdz.taskflow.model.vo;

import lombok.Data;

/**
 * 单个设置项。
 */
@Data
public class ConfigurationVo {

    private Long id;

    /** 设置内容 / 名称 */
    private String content;

    /** 设置说明 */
    private String description;

    /** 设置值 */
    private String value;
}
