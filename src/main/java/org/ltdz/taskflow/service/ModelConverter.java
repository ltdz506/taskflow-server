package org.ltdz.taskflow.service;

import org.ltdz.taskflow.model.po.AiResponsePo;
import org.ltdz.taskflow.model.po.AssetPo;
import org.ltdz.taskflow.model.po.TaskPo;
import org.ltdz.taskflow.model.po.TaskflowPo;
import org.ltdz.taskflow.model.vo.AiResponseVo;
import org.ltdz.taskflow.model.vo.AssetVo;
import org.ltdz.taskflow.model.vo.TaskVo;
import org.ltdz.taskflow.model.vo.TaskflowVo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PO → VO 转换（时间统一格式化为 yyyy-MM-dd HH:mm:ss）。
 */
@Component
public class ModelConverter {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TaskflowVo toTaskflowVo(TaskflowPo po) {
        TaskflowVo vo = new TaskflowVo();
        vo.setId(po.getId());
        vo.setTitle(po.getTitle());
        vo.setStartPrompt(po.getStartPrompt());
        vo.setStatus(po.getStatus());
        vo.setCreateTime(format(po.getCreateTime()));
        vo.setStartTime(format(po.getStartTime()));
        vo.setEndTime(format(po.getEndTime()));
        return vo;
    }

    public TaskVo toTaskVo(TaskPo po) {
        TaskVo vo = new TaskVo();
        vo.setId(po.getId());
        vo.setTaskflowId(po.getTaskflowId());
        vo.setTitle(po.getTitle());
        vo.setRequest(po.getRequest());
        vo.setOrder(po.getOrder());
        vo.setStatus(po.getStatus());
        vo.setSettleTime(format(po.getSettleTime()));
        return vo;
    }

    public AiResponseVo toAiResponseVo(AiResponsePo po) {
        AiResponseVo vo = new AiResponseVo();
        vo.setId(po.getId());
        vo.setTaskId(po.getTaskId());
        vo.setResponse(po.getResponse());
        vo.setRespTime(format(po.getRespTime()));
        return vo;
    }

    public AssetVo toAssetVo(AssetPo po) {
        AssetVo vo = new AssetVo();
        vo.setId(po.getId());
        vo.setIsInput(po.getIsInput());
        vo.setTitle(po.getTitle());
        vo.setSuffix(po.getSuffix());
        vo.setAddress(po.getAddress());
        return vo;
    }

    public String format(LocalDateTime time) {
        return time == null ? null : TIME_FORMATTER.format(time);
    }
}
