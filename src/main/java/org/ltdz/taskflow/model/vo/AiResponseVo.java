package org.ltdz.taskflow.model.vo;

import lombok.Data;

import java.util.List;

/**
 * AI 回复信息（与任务一对一）。
 */
@Data
public class AiResponseVo {

    private Long id;

    private Long taskId;

    private String response;

    /** 回复时间（yyyy-MM-dd HH:mm:ss） */
    private String respTime;

    /** 该回复关联的输出文件 */
    private List<AssetVo> assets;
}
