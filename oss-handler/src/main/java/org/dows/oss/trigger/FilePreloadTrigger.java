package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssFileHandler;
import org.dows.oss.request.OssUploadCallbackRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文件预加载触发
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FilePreloadTrigger implements FileTrigger{

    private final Map<String, FileCallback> fileCallbackMap;
    private final OssFileHandler ossFileHandler;

    public void trigger(OssFileEntity ossFile, OssTriggerEntity ossTriggerEntity, String channel) {
        log.info("文件预上传触发器：{}", ossTriggerEntity.getTrigger());

        ossFile = ossFileHandler.saveOssFile(ossFile);
        String callbackTarget = ossTriggerEntity.getCallbackTarget();
        if (!callbackTarget.isEmpty()) {
            String[] split = callbackTarget.split(":");

            FileCallback fileCallback = getFileCallback(split[0]);
            fileCallback.callback(toOssUploadResponse(ossFile), ossTriggerEntity);
        }
    }

    @Override
    public FileCallback getFileCallback(String callbackType) {
        return fileCallbackMap.get(callbackType + "FileCallback");
    }

    private OssUploadCallbackRequest toOssUploadResponse(OssFileEntity ossFileEntity){
        OssUploadCallbackRequest response = new OssUploadCallbackRequest();
        response.setOssFileId(ossFileEntity.getOssFileId());
        response.setFileName(ossFileEntity.getFileName());
        response.setAppId(ossFileEntity.getAppId());
        response.setBatchNo(ossFileEntity.getBatchNo());
        return response;
    }
}
