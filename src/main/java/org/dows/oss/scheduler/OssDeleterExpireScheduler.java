package org.dows.oss.scheduler;

import com.mybatisflex.core.paginate.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.biz.OssFileBiz;
import org.dows.oss.pojo.enums.OssUploaderStateCodeEnum;
import org.dows.oss.reponse.QueryWaitDeleteResponse;
import org.dows.oss.request.QueryWaitDeleteRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Configuration
@EnableScheduling
@Slf4j
public class OssDeleterExpireScheduler {

    private final AtomicInteger currentPage = new AtomicInteger(1);
    private static final int PAGE_SIZE = 100; // 5线程*10条/线程
    private static final int EXECUTE_NUM = 20; // 每个线程执行的条数
    private Date startTime;
    private Date endTime;
    private final OssFileBiz ossFileBiz;

    /**
     * 删除过期服务器文件任务线程池,同时最大5个并发处理
     */
    @Bean
    public ThreadPoolTaskExecutor deleterExpireTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("deleter-expire-worker-");
        executor.initialize();
        return executor;
    }

    @Scheduled(cron = "${dows.oss.scheduler.deleter.expire:0 0 1 * * ?}")
    public void deleteExpireFile() {
        long oneDayMillis = 24 * 60 * 60 * 1000L;   // 一天的毫秒数
        startTime = new Date(System.currentTimeMillis() - oneDayMillis);
        endTime = new Date(); // 记录任务开始时间作为截止时间

        ThreadPoolTaskExecutor executor = deleterExpireTaskExecutor();
        try {
            while (true) {
                QueryWaitDeleteRequest request = buildRequest();
                Page<QueryWaitDeleteResponse> page = ossFileBiz.queryWaitDeleteFiles(request);

                if (page.getRecords().isEmpty()) break;
                processBatch(executor, page.getRecords());

                if (!page.hasNext()) break;
                currentPage.incrementAndGet();
            }
        } catch (Exception e) {
            log.error("删除过期文件调度异常", e);
        } finally {
            currentPage.set(1);
        }
    }

    private QueryWaitDeleteRequest buildRequest() {
        QueryWaitDeleteRequest request = new QueryWaitDeleteRequest();
        request.setState(OssUploaderStateCodeEnum.COMPLETE_HANDLE.getCode());
        request.setPageNum(currentPage.get());
        request.setPageSize(PAGE_SIZE);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        return request;
    }

    private void processBatch(ThreadPoolTaskExecutor executor, List<QueryWaitDeleteResponse> records)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch((int) Math.ceil((double) records.size() / EXECUTE_NUM));

        for (int i = 0; i < records.size(); i += EXECUTE_NUM) {
            int end = Math.min(i + EXECUTE_NUM, records.size());
            List<QueryWaitDeleteResponse> tempRecords = records.subList(i, end);
            executor.execute(() -> {
                try {
                    ossFileBiz.deleteLocalFile(tempRecords);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }
}
