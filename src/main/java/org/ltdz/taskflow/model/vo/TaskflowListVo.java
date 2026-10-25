package org.ltdz.taskflow.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 任务流列表返回：总数 + 当前页数据。
 */
@Data
public class TaskflowListVo {

    private Long total;

    private List<TaskflowVo> list;
}
