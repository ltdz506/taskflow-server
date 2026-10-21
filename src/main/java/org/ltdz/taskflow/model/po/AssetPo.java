package org.ltdz.taskflow.model.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 附件表实体（asset）。
 */
@Data
public class AssetPo {

    /** 附件 id（后端生成） */
    private Long id;

    /** 归属任务 id */
    private Long taskId;

    /** 是否输入文件：1 输入，0 输出 */
    private Integer isInput;

    /** 文件名 */
    private String title;

    /** 文件后缀（不含点） */
    private String suffix;

    /** 服务端绝对路径 */
    private String address;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
