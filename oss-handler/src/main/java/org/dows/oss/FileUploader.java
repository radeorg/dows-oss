package org.dows.oss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssTriggerHandler;
import org.dows.oss.trigger.FileTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 文件上传
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploader {
    private final Map<String, FileTrigger> fileTriggerMap;

    private final Executor fileUploadTaskExecutor;

    private final OssTriggerHandler ossTriggerHandler;


    /**
     * 文件上传器
     *
     * @param object
     */
    public void upload(Object object) {
        log.info("文件上传");
        // todo 执行文件上传

        // 根据secretId和secretKey获取当前应用配置的触发器,这个地方增加一下注解缓存
        List<OssTriggerEntity> ossTriggerEntities = ossTriggerHandler.triggerList("secretId", "secretKey");

        for (OssTriggerEntity ossTriggerEntity : ossTriggerEntities) {
            FileTrigger fileTrigger = fileTriggerMap.get(ossTriggerEntity.getTrigger());
            fileUploadTaskExecutor.execute(() -> {
                //fileTrigger.trigger(object, ossTriggerEntity);
            });
        }


        // 创建


    }

}
