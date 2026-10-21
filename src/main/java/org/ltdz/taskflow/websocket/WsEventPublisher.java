package org.ltdz.taskflow.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public void taskflowStarted(Long userId, Long taskflowId, String status, LocalDateTime startTime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskflowId", taskflowId);
        data.put("status", status);
        data.put("startTime", format(startTime));
        publish(userId, "taskflow.started", data);
    }

    /**
     * 任务被取用：等待 → 运行
     */
    public void taskRunning(Long userId, Long taskflowId, Long taskId, String status, LocalDateTime settleTime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskflowId", taskflowId);
        data.put("taskId", taskId);
        data.put("status", status);
        data.put("settleTime", format(settleTime));
        publish(userId, "task.running", data);
    }

    /**
     * 任务被回复：运行 → 终止（同时产生 AI 回复）
     */
    public void taskResponded(Long userId, Long taskflowId, Long taskId, String status,
                              String response, LocalDateTime respTime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskflowId", taskflowId);
        data.put("taskId", taskId);
        data.put("status", status);
        data.put("response", response);
        data.put("respTime", format(respTime));
        publish(userId, "task.responded", data);
    }

    /**
     * Agent 上传了输出文件
     */
    public void assetAdded(Long userId, Long taskflowId, Long taskId,
                           Long assetId, Integer isInput, String title, String suffix,
                           LocalDateTime updateTime) {
        Map<String, Object> asset = new LinkedHashMap<>();
        asset.put("id", assetId);
        asset.put("isInput", isInput);
        asset.put("title", title);
        asset.put("suffix", suffix);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskflowId", taskflowId);
        data.put("taskId", taskId);
        data.put("asset", asset);
        data.put("updateTime", format(updateTime));
        publish(userId, "asset.added", data);
    }

    /**
     * 最后一个任务被回复：任务流启动 → 完成
     */
    public void taskflowCompleted(Long userId, Long taskflowId, String status, LocalDateTime endTime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskflowId", taskflowId);
        data.put("status", status);
        data.put("endTime", format(endTime));
        publish(userId, "taskflow.completed", data);
    }

    /**
     * 任务流被销毁（Agent 主动或服务端超时）
     */
    public void taskflowDestroyed(Long userId, Long taskflowId, String status, LocalDateTime endTime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskflowId", taskflowId);
        data.put("status", status);
        data.put("endTime", format(endTime));
        publish(userId, "taskflow.destroyed", data);
    }

    /**
     * 统一组装 {event, time, data} 并发送；失败只记日志，不影响业务。
     */
    private void publish(Long userId, String event, Map<String, Object> data) {
        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("event", event);
            message.put("time", LocalDateTime.now().format(TIME_FORMATTER));
            message.put("data", data);
            sessionManager.sendToUser(userId, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.warn("WebSocket 事件推送失败：event={}, userId={}", event, userId, e);
        }
    }

    private String format(LocalDateTime time) {
        return time == null ? null : TIME_FORMATTER.format(time);
    }
}
