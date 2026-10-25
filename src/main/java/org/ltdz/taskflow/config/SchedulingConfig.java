package org.ltdz.taskflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 开启定时任务（超时兜底、附件清理）。
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
