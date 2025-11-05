package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.request.OssUploadCallbackRequest;
import org.springframework.stereotype.Component;

/**
 * 文件预加载触发
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RepeatFilePreloadTrigger implements FileTrigger{

    public void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        log.info("文件预上传回调业务系统触发器：{}", ossTriggerEntity.getTrigger());

        String callbackTarget = ossTriggerEntity.getCallbackTarget();
        if (!callbackTarget.isEmpty()) {
            String[] split = callbackTarget.split(":");

            FileCallback fileCallback = getFileCallback(split[0]);
            fileCallback.callback(toOssUploadResponse(ossFile), ossTriggerEntity);
        }
    }

    private OssUploadCallbackRequest toOssUploadResponse(OssFileEntity ossFileEntity){
        OssUploadCallbackRequest response = new OssUploadCallbackRequest();
        response.setUploadType(ossFileEntity.getUploadType());
        response.setMd5(ossFileEntity.getMd5());
        response.setOssFileId(ossFileEntity.getOssFileId());
        response.setFileName(ossFileEntity.getFileName());
        response.setAppId(ossFileEntity.getAppId());
        response.setBatchNo(ossFileEntity.getBatchNo());
        response.setOperatorId(ossFileEntity.getOperatorId());
        return response;
    }
}
