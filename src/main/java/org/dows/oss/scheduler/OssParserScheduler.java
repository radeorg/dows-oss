package org.dows.oss.scheduler;

import com.mybatisflex.core.paginate.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.dows.oss.biz.OssFileHandleBiz;
import org.dows.oss.constant.OssUploaderStateCodeConstant;
import org.dows.oss.reponse.QuerySchedulerOssUploadResponse;
import org.dows.oss.request.QuerySchedulerOssUploadRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@RequiredArgsConstructor
@Configuration
@EnableScheduling
@Slf4j
public class OssParserScheduler {

    private final OssFileHandleBiz ossFileHandleBiz;
    private boolean isDatabaseReady = false;

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        // 当应用上下文刷新完成，认为数据库连接已建立
        isDatabaseReady = true;
    }

    /**
     * 服务器文件解析至COS任务线程池,同时最大5个并发处理
     */
    @Bean
    public ThreadPoolTaskExecutor parserTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }

    /**
     * 执行周期性或定时任务线程池
     */
    @Bean(name = "parserScheduledExecutorService", destroyMethod = "shutdown")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t == null && r instanceof Future<?>) {
                    try {
                        Future<?> future = (Future<?>) r;
                        if (future.isDone()) {
                            future.get();
                        }
                    } catch (CancellationException ce) {
                        t = ce;
                    } catch (ExecutionException ee) {
                        t = ee.getCause();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (t != null) {
                    log.error(t.getMessage(), t);
                }
            }
        };
    }

    @Scheduled(fixedRateString = "${dows.oss.scheduler.parser}",timeUnit = TimeUnit.MINUTES)
    public void parseFileToCos() {
//        if (isDatabaseReady) {
//            try {
//                QuerySchedulerOssUploadRequest request = new QuerySchedulerOssUploadRequest();
//                request.setStateCode("_0"); // 第二位为0表示未解析
//                request.setStateCodeType(OssUploaderStateCodeConstant.STATE_TYPE_RIGHT_LIKE);
//                Page<QuerySchedulerOssUploadResponse> page = ossFileHandleBiz.queryLocalFile(request);
//                ossFileHandleBiz.parseLocalFileToCos(page.getRecords());
//            }catch (Exception e){
//                log.error("解析文件至COS异常： " + e.getLocalizedMessage());
//                e.printStackTrace();
//            }
//        }
    }
}
