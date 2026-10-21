package org.ltdz.taskflow.model.po;

import lombok.Data;

/**
 * 设置项表实体（configuration）。
 */
@Data
public class ConfigurationPo {

    /** 设置 id（后端生成） */
    private Long id;

    /** 归属用户 id */
    private Long userId;

    /** 设置内容 / 名称 */
    private String content;

    /** 设置说明 */
    private String description;

    /** 是否生效：是否用该值覆盖后端默认值 */
    private Integer isLived;

    /** 是否可被用户编辑 */
    private Integer canBeSet;

    /** 设置值 */
    private String value;

    /** 设置项顺序 */
    private Integer order;
}
