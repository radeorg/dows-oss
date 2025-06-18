package org.dows.oss.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.FileUploader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author tangsm
 * @data 2025/6/18 星期三
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteExpireFileScheduler {

    private final FileUploader fileUploader;

    // 每天00:00:00执行（CRON表达式）
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyTask() {
        log.info("DeleteExpireFileScheduler定时任务执行开始: {}", System.currentTimeMillis());

        fileUploader.deleteExpireLocalFile();

        log.info("DeleteExpireFileScheduler定时任务执行结束: {}", System.currentTimeMillis());
    }
}
