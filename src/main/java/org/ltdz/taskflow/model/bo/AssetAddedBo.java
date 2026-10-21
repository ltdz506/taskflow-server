package org.ltdz.taskflow.model.bo;

import lombok.Data;
import org.ltdz.taskflow.model.vo.AssetVo;

/**
 * 事件 asset.added 的载荷：Agent 上传了输出文件。
 */
@Data
public class AssetAddedBo {

    private Long taskflowId;

    private Long taskId;

    private AssetVo asset;

    private String updateTime;
}
