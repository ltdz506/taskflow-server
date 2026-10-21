package org.ltdz.taskflow.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ltdz.taskflow.model.bo.AssetAddedBo;
import org.ltdz.taskflow.model.bo.TaskRespondedBo;
import org.ltdz.taskflow.model.bo.TaskRunningBo;
import org.ltdz.taskflow.model.bo.TaskflowCompletedBo;
import org.ltdz.taskflow.model.bo.TaskflowDestroyedBo;
import org.ltdz.taskflow.model.bo.TaskflowStartedBo;
import org.ltdz.taskflow.model.bo.WsEventBo;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebSocket 事件推送：把业务变化包装成统一消息，推给任务流所属用户。
 * <p>
 * 消息格式：{event, time, data}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsEventPublisher {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WsSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * 任务流被领取：编辑 → 启动
     */
    public void taskflowStarted(Long userId, TaskflowStartedBo data) {
        publish(userId, "taskflow.started", data);
    }

    /**
     * 任务被取用：等待 → 运行
     */
    public void taskRunning(Long userId, TaskRunningBo data) {
        publish(userId, "task.running", data);
    }

    /**
     * 任务被回复：运行 → 终止（同时产生 AI 回复）
     */
    public void taskResponded(Long userId, TaskRespondedBo data) {
        publish(userId, "task.responded", data);
    }

    /**
     * Agent 上传了输出文件
     */
    public void assetAdded(Long userId, AssetAddedBo data) {
        publish(userId, "asset.added", data);
    }

    /**
     * 最后一个任务被回复：任务流启动 → 完成
     */
    public void taskflowCompleted(Long userId, TaskflowCompletedBo data) {
        publish(userId, "taskflow.completed", data);
    }

    /**
     * 任务流被销毁（Agent 主动或服务端超时）
     */
    public void taskflowDestroyed(Long userId, TaskflowDestroyedBo data) {
        publish(userId, "taskflow.destroyed", data);
    }

    /**
     * 统一组装 {event, time, data} 并发送；失败只记日志，不影响业务。
     */
    private <T> void publish(Long userId, String event, T data) {
        try {
            WsEventBo<T> message = new WsEventBo<>();
            message.setEvent(event);
            message.setTime(LocalDateTime.now().format(TIME_FORMATTER));
            message.setData(data);
            sessionManager.sendToUser(userId, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.warn("WebSocket 事件推送失败：event={}, userId={}", event, userId, e);
        }
    }
}
