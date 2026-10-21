package org.ltdz.taskflow.model.vo;

import lombok.Data;

/**
 * 附件信息（不含文件内容，下载走单独接口）。
 */
@Data
public class AssetVo {

    private Long id;

    /** 1 输入文件，0 输出文件 */
    private Integer isInput;

    private String title;

    private String suffix;

    /** 服务端绝对路径（前端不可直接访问） */
    private String address;
}
