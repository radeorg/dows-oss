package org.dows.oss;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssIdentifierEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssTriggerHandler;
import org.dows.oss.request.OssUploadInputStreamRequest;
import org.dows.oss.request.OssUploadRequest;
import org.dows.oss.service.OssDetailService;
import org.dows.oss.service.OssFileService;
import org.dows.oss.trigger.FilePreloadTrigger;
import org.dows.oss.trigger.FileTrigger;
import org.dows.oss.utils.CommonUtil;
import org.dows.rade.context.AppContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * 文件上传
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploader {

    @Value("${rade.oss.path:/radeorg}")
    private String orgPath;

    private final Map<String, FileTrigger> fileTriggerMap;

    private final Executor fileUploadTaskExecutor;

    private final OssTriggerHandler ossTriggerHandler;
    private final OssFileService ossFileService;
    private final OssDetailService ossDetailService;
    private final FilePreloadTrigger filePreloadTrigger;

    /**
     * 文件上传器
     */
    public Map<String, Object> upload(OssUploadRequest ossUploadRequest) {
        log.info("文件上传");
        OssIdentifierEntity ossIdentifierEntity = validateOssIdentifier(ossUploadRequest.getSource());

        return uploadFileToLocal(ossUploadRequest, ossIdentifierEntity);
    }

    /**
     * 文件上传器
     */
    public void upload(InputStream is, OssUploadInputStreamRequest request) {
        log.info("文件流上传");
        OssIdentifierEntity ossIdentifierEntity = validateOssIdentifier(request.getSource());

        OssFileEntity ossFile = ossFileService.getOne(QueryWrapper.create().eq(OssFileEntity::getMd5, request.getMd5()));
        if (ossFile != null) {
            String originalFileName = request.getFileName();
            String targetDirectory = getTargetDirectory(request.getSource());
            String targetFilePath = getFilePath(targetDirectory, originalFileName, request.getMd5());
            File dest = new File(targetFilePath);
            File parentDir = dest.getParentFile();
            try {
                if (!parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        throw new IOException("目录创建失败: " + parentDir.getAbsolutePath());
                    }
                }
                FileUtil.writeFromStream(is, dest);

                // 执行触发器
                OssUploadRequest.OssUploadInfo info = new OssUploadRequest.OssUploadInfo();
                info.setMd5(request.getMd5());

                trigger(info, ossIdentifierEntity, targetFilePath, originalFileName, dest.length());
            } catch (Exception e) {
                log.error("文件流上传失败: {}", originalFileName, e);
                if (dest.exists() && !dest.delete()) {
                    log.error("文件流上传删除失败: {}", dest.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 上传文件到服务器
     */
    private Map<String, Object> uploadFileToLocal(OssUploadRequest ossUploadRequest, OssIdentifierEntity ossIdentifierEntity){
        int successNum = 0;
        Map<String, String> failInfo = new HashMap<>();
        List<String> distinctMd5s = new ArrayList<>();
        List<String> existUploaderFileMd5 = queryExistUploaderFileMd5(ossUploadRequest);
        for (OssUploadRequest.OssUploadInfo info : ossUploadRequest.getInfos()) {
            String md5 = info.getMd5();
            if (!distinctMd5s.contains(md5) && !existUploaderFileMd5.contains(md5)) {
                String fileName = getFileName(info.getFile());
                String targetDirectory = getTargetDirectory(ossUploadRequest.getSource());
                String filePath = getFilePath(targetDirectory, fileName, md5);
                File dest = new File(filePath);
                File parentDir = dest.getParentFile();
                try {
                    if (!parentDir.exists()) {
                        if (!parentDir.mkdirs()) {
                            throw new IOException("目录创建失败: " + parentDir.getAbsolutePath());
                        }
                    }
                    info.getFile().transferTo(dest);

                    // 执行触发器
                    trigger(info, ossIdentifierEntity, filePath, fileName, dest.length());

                    successNum++;
                } catch (Exception e) {
                    log.error("文件上传失败: {}", fileName, e);
                    if (dest.exists() && !dest.delete()) {
                        log.error("文件删除失败: {}", dest.getAbsolutePath());
                    }
                    failInfo.put(info.getMd5(), e.getMessage());
                }
            } else {
                failInfo.put(info.getMd5(), "文件重复");
            }
            if (!distinctMd5s.contains(md5)) {
                distinctMd5s.add(md5);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalNum", ossUploadRequest.getInfos().size());
        result.put("successNum", successNum);
        result.put("failNum", ossUploadRequest.getInfos().size() - successNum);
        result.put("failInfo", failInfo);
        return result;
    }

    /**
     * 查询在数据库中存在的Md5
     */
    private List<String> queryExistUploaderFileMd5(OssUploadRequest ossUploadRequest){
        // 过滤出不重复、不为空的md5集合
        List<String> md5s = ossUploadRequest.getInfos().stream()
                .map(OssUploadRequest.OssUploadInfo::getMd5)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> fileMd5s = new ArrayList<>();
        List<OssFileEntity> ossFiles = ossFileService.list(QueryWrapper.create().in(OssFileEntity::getMd5, md5s));
        if (ossFiles != null && !ossFiles.isEmpty()) {
            for (OssFileEntity ossUploaderEntity : ossFiles) {
                fileMd5s.add(ossUploaderEntity.getMd5());
            }
        }
        return fileMd5s;
    }

    private OssIdentifierEntity validateOssIdentifier(String source){
        OssIdentifierEntity ossIdentifierEntity = ossTriggerHandler.getOssIdentifierBySourceAndAppId(source, AppContext.getAppId());
        if (ossIdentifierEntity == null) {
            throw new IllegalArgumentException("未找到对应的OSS配置");
        }
        return ossIdentifierEntity;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExt(String fileName){
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 获取文件目标存储目录
     */
    private String getTargetDirectory(String directory){
        return System.getProperty("user.home")
                + orgPath + File.separator
                + directory + File.separator
                + CommonUtil.formatDate(new Date(), "yyMMdd");
    }

    /**
     * 获取文件存储路径
     * targetDirectory 文件目标存储目录
     * oldFileName 原始文件名（带扩展名）
     * newFileName   新文件名（不带扩展名）
     */
    private String getFilePath(String targetDirectory, String oldFileName, String newFileName){
        return targetDirectory + File.separator + newFileName + getFileExt(oldFileName);
    }

    private String getFileName(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        return fileName;
    }

    private void trigger(OssUploadRequest.OssUploadInfo info, OssIdentifierEntity ossIdentifier, String filePath, String fileName, Long fileSize) {
        List<OssTriggerEntity> ossTriggerEntities = ossTriggerHandler.triggerList(ossIdentifier.getOssIdentifierId());
        if (ossTriggerEntities != null) {
            OssFileEntity ossFile = new OssFileEntity();
            for (OssTriggerEntity ossTriggerEntity : ossTriggerEntities) {
                if (ossTriggerEntity != null) {
                    FileTrigger fileTrigger = fileTriggerMap.get(ossTriggerEntity.getTrigger());
                    if (StrUtil.lowerFirst(fileTrigger.getClass().getName()).equals(ossTriggerEntity.getTrigger())) {
                        ossFile = saveOssFile(info, ossIdentifier, filePath, fileName, fileSize);
                        filePreloadTrigger.trigger(ossFile, null, ossTriggerEntity);
                    } else {
                        OssDetailEntity ossDetail = saveOssDetail(ossFile, ossTriggerEntity, ossIdentifier.getChannel());
                        OssFileEntity finalOssFile = ossFile;
                        fileUploadTaskExecutor.execute(() -> {
                            fileTrigger.trigger(finalOssFile, ossDetail, ossTriggerEntity);
                        });
                    }
                }
            }
        }
    }

    private OssFileEntity saveOssFile(OssUploadRequest.OssUploadInfo info, OssIdentifierEntity ossIdentifierEntity, String filePath, String fileName, Long fileSize){
        OssFileEntity entity = new OssFileEntity();
        entity.setFileName(fileName);
        entity.setMd5(info.getMd5());
        entity.setFileExt(getFileExt(fileName));
        entity.setFileSize(fileSize);
        entity.setFileTempPath(filePath);
        entity.setBatchNo(CommonUtil.formatDate(new Date(), "yyMMddHH"));
        entity.setAppId(ossIdentifierEntity.getAppId());
        entity.setSource(ossIdentifierEntity.getSource());
        ossFileService.save(entity);
        return entity;
    }

    private OssDetailEntity saveOssDetail(OssFileEntity ossFile, OssTriggerEntity trigger, String channel) {
        OssDetailEntity detailEntity = new OssDetailEntity();
        detailEntity.setOssFileId(ossFile.getOssFileId());
        detailEntity.setAppId(trigger.getAppId());
        detailEntity.setTrigger(trigger.getTrigger());
        detailEntity.setChannel(channel);
        detailEntity.setBasePath(trigger.getBasePath());
        detailEntity.setSeq(trigger.getSeq());
        detailEntity.setRetryCount(trigger.getRetryCount());
        ossDetailService.save(detailEntity);
        return detailEntity;
    }
}
