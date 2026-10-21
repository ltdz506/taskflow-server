package org.ltdz.taskflow.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 创建任务流的返回。
 */
@Data
public class CreateTaskflowVo {

    private Long taskflowId;

    /** 各任务 id，顺序与请求 tasks 一致 */
    private List<Long> taskIds;
}
