package org.dows.oss.handler;

import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssIdentifierEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.request.OssUploadRequest;
import org.dows.oss.trigger.FileTrigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author tangsm
 * @data 2025/8/12 星期二
 */
@Slf4j
@Component
//@RequiredArgsConstructor
public class FileUploaderTriggerHandler {

    private final ThreadPoolTaskExecutor fileUploadTaskExecutor;
    private final Map<String, FileTrigger> fileTriggerMap;
    private final OssTriggerHandler ossTriggerHandler;
    private final OssFileHandler ossFileHandler;
    private final OssDetailHandler ossDetailHandler;

    /*
        此处通过构造器注入bean原因：
        项目中其他配置类或第三方库自动配置了线程池ThreadPoolTaskExecutor，
        所以需要通过构造方法明确指定注入的Bean名称，否则会报找到多个bean
     */
    public FileUploaderTriggerHandler(Map<String, FileTrigger> fileTriggerMap,
                        @Qualifier("fileUploadTaskExecutor") ThreadPoolTaskExecutor fileUploadTaskExecutor,
                        OssTriggerHandler ossTriggerHandler,
                        OssFileHandler ossFileHandler,
                        OssDetailHandler ossDetailHandler) {
        this.fileTriggerMap = fileTriggerMap;
        this.fileUploadTaskExecutor = fileUploadTaskExecutor;
        this.ossTriggerHandler = ossTriggerHandler;
        this.ossFileHandler = ossFileHandler;
        this.ossDetailHandler = ossDetailHandler;
    }

    @Transactional
    public void trigger(OssUploadRequest.OssUploadInfo info, OssIdentifierEntity ossIdentifier,
                           String filePath, String fileName, Long fileSize) {
        List<OssTriggerEntity> ossTriggerEntities = ossTriggerHandler.triggerList(ossIdentifier.getOssIdentifierId());
        if (ossTriggerEntities != null) {
            OssFileEntity ossFile = new OssFileEntity();
            String tempFilePath = "";
            for (OssTriggerEntity ossTriggerEntity : ossTriggerEntities) {
                if (ossTriggerEntity != null) {
                    FileTrigger fileTrigger = fileTriggerMap.get(ossTriggerEntity.getTrigger());
                    if (ossTriggerEntity.getSeq() == 1) {
                        ossFile = ossFileHandler.saveOssFile(info, ossIdentifier, filePath, fileName, fileSize);
                        fileTrigger.trigger(ossFile, null, ossTriggerEntity);
                    } else {
                        OssDetailEntity ossDetail = ossDetailHandler.saveOssDetail(ossFile, ossTriggerEntity, ossIdentifier.getChannel());
//                        OssFileEntity finalOssFile = ossFile;
//                        fileUploadTaskExecutor.execute(() -> {
                        // 后面的转换Md文件需要上传文件的链接地址
                        ossDetail.setFilePath(tempFilePath);
                        fileTrigger.trigger(ossFile, ossDetail, ossTriggerEntity);
                        if (ossTriggerEntity.getSeq() == 2) {
                            tempFilePath = ossDetail.getFilePath();
                        } else {
                            tempFilePath = "";
                        }
//                        });
                    }
                }
            }
        }
    }

    @Transactional
    public void repeatTrigger(OssUploadRequest.OssUploadInfo info,
                               OssIdentifierEntity ossIdentifier,
                               OssFileEntity oldFile,
                               String fileName) {
        List<OssTriggerEntity> ossTriggerEntities = ossTriggerHandler.triggerList(ossIdentifier.getOssIdentifierId());
        if (ossTriggerEntities != null) {
            OssFileEntity ossFile = new OssFileEntity();
            for (OssTriggerEntity ossTriggerEntity : ossTriggerEntities) {
                if (ossTriggerEntity != null) {
                    String triggerName = ossTriggerEntity.getTrigger();
                    triggerName = "repeat" + triggerName.substring(0, 1).toUpperCase() + triggerName.substring(1);
                    FileTrigger fileTrigger = fileTriggerMap.get(triggerName);
                    if (ossTriggerEntity.getSeq() == 1) {
                        ossFile = ossFileHandler.saveOssFile(info, ossIdentifier,
                                oldFile.getFileTempPath(), fileName, oldFile.getFileSize());
                        fileTrigger.trigger(ossFile, null, ossTriggerEntity);
                    } else {
                        OssDetailEntity ossDetail = ossDetailHandler.saveOssDetail(oldFile, ossFile, ossTriggerEntity, ossIdentifier.getChannel());
//                        fileUploadTaskExecutor.execute(() -> {
                        fileTrigger.trigger(null, ossDetail, ossTriggerEntity);
//                        });
                    }
                }
            }
        }
    }

    @Transactional
    public void recoveryTrigger(OssFileEntity ossFile, OssIdentifierEntity ossIdentifier) {
        List<OssTriggerEntity> ossTriggerEntities = ossTriggerHandler.triggerList(ossIdentifier.getOssIdentifierId());
        if (ossTriggerEntities != null) {
            for (OssTriggerEntity ossTriggerEntity : ossTriggerEntities) {
                if (ossTriggerEntity != null && ossTriggerEntity.getSeq() == 4) {
                    FileTrigger fileTrigger = fileTriggerMap.get(ossTriggerEntity.getTrigger());
                    fileTrigger.trigger(ossFile, null, ossTriggerEntity);
                }
            }
        }
    }
}
