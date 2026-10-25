package org.ltdz.taskflow.model.bo;

import lombok.Data;
import org.springframework.core.io.Resource;

/**
 * 下载用的文件载体：流式资源 + 原始文件名。
 */
@Data
public class AssetFileBo {

    /** 流式文件资源，不读入内存 */
    private Resource resource;

    /** 原始文件名（用于 Content-Disposition） */
    private String title;
}
