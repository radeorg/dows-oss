package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssDetailHandler;
import org.dows.oss.response.CallbackResponse;
import org.springframework.stereotype.Component;


/**
 * 文件上传云服务触发器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RepeatFileUploadedTrigger implements FileTrigger {

    private final OssDetailHandler ossDetailHandler;

    @Override
    public void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        log.info("文件上传回调业务系统触发器：{}", ossTriggerEntity.getTrigger());

        try {
            // 回调业务系统
            CallbackResponse callbackResponse = callback(ossDetail, ossTriggerEntity);

            // 更新文件回调信息
            ossDetailHandler.updateOssDetailCallbackInfo(ossDetail, callbackResponse);
        } catch (Exception e) {
            log.error("文件上传回调业务系统触发器异常:{}", e.getMessage());
        }
    }
}
