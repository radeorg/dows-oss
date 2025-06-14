package org.dows.oss.scheduler;

import com.mybatisflex.core.paginate.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.biz.OssFileBiz;
import org.dows.oss.pojo.enums.OssUploaderCallBackStateEnum;
import org.dows.oss.pojo.enums.OssUploaderStateCodeEnum;
import org.dows.oss.reponse.CallbackBizResponse;
import org.dows.oss.request.QueryWaitCallbackRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Configuration
@EnableScheduling
@Slf4j
public class CallbackBizScheduler {

    private final AtomicInteger currentPage = new AtomicInteger(1);
    private static final int PAGE_SIZE = 100; // 5线程*10条/线程
    private static final int EXECUTE_NUM = 20; // 每个线程执行的条数
    private final OssFileBiz ossFileBiz;

    /**
     * 将已执行完的文件地址通过回调返回给业务系统线程池,同时最大5个并发处理
     */
    @Bean
    public ThreadPoolTaskExecutor callbackBizTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("callback-biz-worker-");
        executor.initialize();
        return executor;
    }

    @Scheduled(fixedRateString = "${dows.oss.scheduler.callbackBiz}",timeUnit = TimeUnit.MINUTES)
    public void parseFileToCos() {
        ThreadPoolTaskExecutor executor = callbackBizTaskExecutor();
        try {
            while (true) {
                QueryWaitCallbackRequest request = buildRequest();
                Page<CallbackBizResponse> page = ossFileBiz.queryWaitCallbackFiles(request);

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

    private QueryWaitCallbackRequest buildRequest() {
        QueryWaitCallbackRequest request = new QueryWaitCallbackRequest();
        request.setState(OssUploaderStateCodeEnum.COMPLETE_HANDLE.getCode());
        request.setCallbackState(OssUploaderCallBackStateEnum.WAIT_CALLBACK.getCode());
        request.setPageNum(currentPage.get());
        request.setPageSize(PAGE_SIZE);
        return request;
    }

    private void processBatch(ThreadPoolTaskExecutor executor, List<CallbackBizResponse> records)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch((int) Math.ceil((double) records.size() / EXECUTE_NUM));

        for (int i = 0; i < records.size(); i += EXECUTE_NUM) {
            int end = Math.min(i + EXECUTE_NUM, records.size());
            List<CallbackBizResponse> tempRecords = records.subList(i, end);
            executor.execute(() -> {
                try {
                    ossFileBiz.callbackBiz(tempRecords);
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
