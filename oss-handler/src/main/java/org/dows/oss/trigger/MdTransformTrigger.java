package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssDetailHandler;
import org.dows.oss.handler.OssUploader;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.rade.oss.OssInfo;
import org.springframework.stereotype.Component;

/**
 * 文件转换触发器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MdTransformTrigger implements FileTrigger {

    private final OssUploader ossUploader;
    private final OssDetailHandler ossDetailHandler;

    @Override
    public void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        log.info("文件转换触发器：{}", ossTriggerEntity.getTrigger());

        try {
            // 文件上传云服务
            OssUploadHandlerRequest request = ossDetailHandler.toOssUploadHandlerRequest(ossFile, ossDetail);
            OssInfo info = ossUploader.uploadParseMarkDownFile(request);

            // 更新文件信息
            ossDetailHandler.updateOssDetail(info, ossDetail);

            // 回调业务系统
            ossDetailHandler.callback(ossDetail, ossTriggerEntity);
        } catch (Exception e) {
            log.error("文件上传云服务触发器异常:{}", e.getMessage());
        }
    }
}
