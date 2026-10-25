package org.ltdz.taskflow.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ltdz.taskflow.mapper.AssetMapper;
import org.ltdz.taskflow.mapper.TaskMapper;
import org.ltdz.taskflow.mapper.TaskflowMapper;
import org.ltdz.taskflow.model.po.TaskflowPo;
import org.ltdz.taskflow.service.ConfigService;
import org.ltdz.taskflow.service.FileStorageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 附件保留清理：每日扫描已结束的任务流，清理超过保留期的附件。
 * <p>
 * 只删附件（磁盘文件 + asset 记录），**任务流 / 任务 / 回复记录全部保留**。
 * 保留期取设置项5（7/30/90 天，值 0 表示永久保留）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetCleanupTask {

    private static final int DEFAULT_RETENTION_DAYS = 30;

    /** 保留期值为 0 时表示永久保留 */
    private static final int FOREVER = 0;

    private final TaskflowMapper taskflowMapper;
    private final TaskMapper taskMapper;
    private final AssetMapper assetMapper;
    private final ConfigService configService;
    private final FileStorageService fileStorageService;

    /**
     * 每日 03:00 执行。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        List<TaskflowPo> finished = taskflowMapper.selectFinished();
        if (finished.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (TaskflowPo taskflow : finished) {
            LocalDateTime endTime = taskflow.getEndTime();
            if (endTime == null) {
                continue;
            }

            int days = configService.getIntValue(
                    taskflow.getUserId(), ConfigService.CONTENT_RETENTION, DEFAULT_RETENTION_DAYS);
            if (days == FOREVER || endTime.plusDays(days).isAfter(now)) {
                continue;
            }

            try {
                List<Long> taskIds = taskMapper.selectIdsByTaskflowId(taskflow.getId());
                if (!taskIds.isEmpty()) {
                    assetMapper.deleteByTaskIds(taskIds);
                }
                fileStorageService.deleteByTaskflow(taskflow.getId());
                log.info("任务流 {} 的附件已按保留期（{} 天）清理", taskflow.getId(), days);
            } catch (Exception e) {
                log.error("任务流 {} 附件清理失败", taskflow.getId(), e);
            }
        }
    }
}
