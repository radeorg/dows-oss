package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssDetailHandler;
import org.dows.oss.handler.OssUploaderHandler;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.rade.oss.OssInfo;
import org.dows.rade.util.SpringUtil;
import org.springframework.stereotype.Component;

/**
 * 文件上传云服务触发器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FileUploadedTrigger implements FileTrigger {

    private final OssUploaderHandler ossUploaderHandler;
    private final OssDetailHandler ossDetailHandler;


    @Override
    public void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        log.info("文件上传云服务触发器：{}", ossTriggerEntity.getTrigger());

        try {
            // 文件上传云服务
            OssUploadHandlerRequest request = ossDetailHandler.toOssUploadHandlerRequest(ossFile, ossDetail);
            OssInfo info = ossUploaderHandler.uploadOriginalFile(request);
            if (info != null) {
                // 更新文件上传链接信息
                ossDetailHandler.updateOssDetailFileInfo(info, ossDetail);

                // 回调业务系统
                CallbackResponse callbackResponse = callback(ossDetail, ossTriggerEntity);

                // 更新文件回调信息
                ossDetailHandler.updateOssDetailCallbackInfo(ossDetail, callbackResponse);
            }
        } catch (Exception e) {
            log.error("文件上传云服务触发器异常:{}", e.getMessage());
        }
    }

    @Override
    public FileCallback getFileCallback(String callbackType) {
        // todo 是否为空判断？
        return SpringUtil.getBean(callbackType + "FileCallback");
    }
}
