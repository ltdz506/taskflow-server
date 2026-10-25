package org.ltdz.taskflow.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ltdz.taskflow.enums.TaskflowStatus;
import org.ltdz.taskflow.mapper.TaskflowMapper;
import org.ltdz.taskflow.model.po.TaskflowPo;
import org.ltdz.taskflow.service.AgentService;
import org.ltdz.taskflow.service.ConfigService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 超时兜底：扫描处于「启动」状态且已超时的任务流，**按 Agent 销毁的逻辑**处理。
 * <p>
 * 任务的单轮超时时间取设置项1（默认 30 分钟）。销毁后任务流为「销毁」态，
 * Agent 即使再调取任务/回复接口也会因为状态校验而被拒绝。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeoutGuardTask {

    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    private static final long SCAN_DELAY_MS = 60_000L;
    private static final long INITIAL_DELAY_MS = 30_000L;

    private final TaskflowMapper taskflowMapper;
    private final ConfigService configService;
    private final AgentService agentService;

    /**
     * 每分钟扫描一次。
     */
    @Scheduled(fixedDelay = SCAN_DELAY_MS, initialDelay = INITIAL_DELAY_MS)
    public void checkTimeout() {
        List<TaskflowPo> running = taskflowMapper.selectByStatus(TaskflowStatus.RUNNING.name());
        if (running.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (TaskflowPo taskflow : running) {
            LocalDateTime startTime = taskflow.getStartTime();
            if (startTime == null) {
                continue;
            }

            int timeoutMinutes = configService.getIntValue(
                    taskflow.getUserId(), ConfigService.CONTENT_TIMEOUT, DEFAULT_TIMEOUT_MINUTES);
            if (startTime.plusMinutes(timeoutMinutes).isAfter(now)) {
                continue;
            }

            try {
                // 复用 Agent 销毁逻辑：任务流 → 销毁（不改任务状态）+ 推送 taskflow.destroyed
                agentService.destroyTaskflowById(taskflow.getUserId(), taskflow.getId());
                log.warn("任务流 {} 超时（{} 分钟），已自动销毁", taskflow.getId(), timeoutMinutes);
            } catch (Exception e) {
                log.error("任务流 {} 超时销毁失败", taskflow.getId(), e);
            }
        }
    }
}
