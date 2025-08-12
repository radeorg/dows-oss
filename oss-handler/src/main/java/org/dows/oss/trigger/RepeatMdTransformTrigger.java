package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.handler.OssDetailHandler;
import org.dows.oss.handler.OssUploaderHandler;
import org.dows.oss.response.CallbackResponse;
import org.springframework.stereotype.Component;

/**
 * 文件转换触发器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RepeatMdTransformTrigger implements FileTrigger {

    private final OssDetailHandler ossDetailHandler;
    private final OssUploaderHandler ossUploaderHandler;

    @Override
    public void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity)  {
        log.info("MD文件回调业务系统触发器：{}", ossTriggerEntity.getTrigger());

        try {
            String parseContent = ossUploaderHandler.downloadFile(ossDetail.getFilePath());

            // 回调业务系统
            CallbackResponse callbackResponse = callback(ossDetail, ossTriggerEntity, parseContent);

            // 更新文件回调信息
            ossDetailHandler.updateOssDetailCallbackInfo(ossDetail, callbackResponse);
        } catch (Exception e) {
            log.error("MD文件回调业务系统触发器异常:{}", e.getMessage());
            throw new OssFileException(e.getMessage());
        }
    }
}
