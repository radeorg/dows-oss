package org.dows.oss.scheduler;

import com.mybatisflex.core.paginate.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.biz.OssFileHandleBiz;
import org.dows.oss.constant.OssUploaderStateCodeConstant;
import org.dows.oss.reponse.QuerySchedulerOssUploadResponse;
import org.dows.oss.request.QuerySchedulerOssUploadRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Configuration
@EnableScheduling
@Slf4j
public class OssUploaderScheduler {

    private final AtomicInteger currentPage = new AtomicInteger(1);
    private static final int PAGE_SIZE = 100; // 5线程*10条/线程
    private static final int EXECUTE_NUM = 20; // 每个线程执行的条数
    private final OssFileHandleBiz ossFileHandleBiz;

    /**
     * 服务器文件上传至COS任务线程池,同时最大5个并发处理
     */
    @Bean
    public ThreadPoolTaskExecutor uploaderTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("upload-worker-");
        executor.initialize();
        return executor;
    }

    @Scheduled(fixedRateString = "${dows.oss.scheduler.uploader:30}", timeUnit = TimeUnit.MINUTES)
    public void uploadLocalFileToCos() {
        ThreadPoolTaskExecutor executor = uploaderTaskExecutor();
        try {
            while (true) {
                QuerySchedulerOssUploadRequest request = buildRequest();
                Page<QuerySchedulerOssUploadResponse> page = ossFileHandleBiz.queryLocalFile(request);

                if (page.getRecords().isEmpty()) break;
                processBatch(executor, page.getRecords());

                if (!page.hasNext()) break;
                currentPage.incrementAndGet();
            }
        } catch (Exception e) {
            log.error("文件上传调度异常", e);
        } finally {
            currentPage.set(1);
        }
    }

    private QuerySchedulerOssUploadRequest buildRequest() {
        QuerySchedulerOssUploadRequest request = new QuerySchedulerOssUploadRequest();
        request.setStateCode("0"); // 第一位状态码为0代表未上传
        request.setStateCodeType(OssUploaderStateCodeConstant.STATE_TYPE_LEFT_LIKE);
        request.setPageNum(currentPage.get());
        request.setPageSize(PAGE_SIZE);
        return request;
    }

    private void processBatch(ThreadPoolTaskExecutor executor, List<QuerySchedulerOssUploadResponse> records)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch((int) Math.ceil((double) records.size() / EXECUTE_NUM));

        for (int i = 0; i < records.size(); i += EXECUTE_NUM) {
            int end = Math.min(i + EXECUTE_NUM, records.size());
            List<QuerySchedulerOssUploadResponse> tempRecords = records.subList(i, end);
            executor.execute(() -> {
                try {
                    ossFileHandleBiz.uploadLocalFileToCos(tempRecords);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }
}
