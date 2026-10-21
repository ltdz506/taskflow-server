package org.ltdz.taskflow.model.vo;

import lombok.Data;

/**
 * 上传附件后的返回。
 */
@Data
public class UploadAssetVo {

    private Long id;

    private String title;

    private String suffix;
}
