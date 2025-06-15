package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssUploader;
import org.dows.oss.service.OssFileService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文件上传成功触发器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FileUploadedTrigger implements FileTrigger {

    private final Map<String, FileCallback> fileCallbackMap;

    private final OssUploader ossUploader;

    private final OssFileService ossFileService;

    @Override
    public void trigger(Long ossFileId, Object object, OssTriggerEntity ossTriggerEntity) {

        //ossFileService.query().eq(OssFileEntity::getOssUploaderId, ossTriggerEntity.getOssUploaderId())
        //Long ossFileId = null;
        //todo 处理object 上传
        ossUploader.upload(object);
        // todo 触发
        log.info("文件上传成功触发器：{}", ossTriggerEntity.getTrigger());
        String callbackTarget = ossTriggerEntity.getCallbackTarget();
        // bean://pkg.class#method,http://url,jdbc://sql...
        if (callbackTarget.isEmpty()) {
            return;
        }
        String[] split = callbackTarget.split(":");

        FileCallback fileCallback = fileCallbackMap.get(split[0] + "FileCallback");

        fileCallback.callback(null, ossTriggerEntity);
    }
}
