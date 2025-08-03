package org.dows.oss;

import com.qcloud.cos.utils.IOUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.dows.oss.constant.OssExceptionStatusCode;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssIdentifierEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssDetailHandler;
import org.dows.oss.handler.OssFileHandler;
import org.dows.oss.handler.OssTriggerHandler;
import org.dows.oss.handler.OssUploaderHandler;
import org.dows.oss.request.OssUploadInputStreamRequest;
import org.dows.oss.request.OssUploadRequest;
import org.dows.oss.trigger.FileTrigger;
import org.dows.oss.utils.CommonUtil;
import org.dows.oss.utils.FileParseUtil;
import org.dows.rade.context.AppContext;
import org.dows.rade.oss.OssException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件上传
 */
@Slf4j
@Component
//@RequiredArgsConstructor
public class FileUploader {

    @Value("${rade.oss.path:/radeorg}")
    private String orgPath;

    private final Map<String, FileTrigger> fileTriggerMap;
    private final ThreadPoolTaskExecutor fileUploadTaskExecutor;
    private final OssTriggerHandler ossTriggerHandler;
    private final OssFileHandler ossFileHandler;
    private final OssDetailHandler ossDetailHandler;
    private final OssUploaderHandler ossUploaderHandler;

    /*
        此处通过构造器注入bean原因：
        项目中其他配置类或第三方库自动配置了线程池ThreadPoolTaskExecutor，
        所以需要通过构造方法明确指定注入的Bean名称，否则会报找到多个bean
     */
    public FileUploader(Map<String, FileTrigger> fileTriggerMap,
                        @Qualifier("fileUploadTaskExecutor") ThreadPoolTaskExecutor fileUploadTaskExecutor,
                        OssTriggerHandler ossTriggerHandler,
                        OssFileHandler ossFileHandler,
                        OssDetailHandler ossDetailHandler,
                        OssUploaderHandler ossUploaderHandler) {
        this.fileTriggerMap = fileTriggerMap;
        this.fileUploadTaskExecutor = fileUploadTaskExecutor;
        this.ossTriggerHandler = ossTriggerHandler;
        this.ossFileHandler = ossFileHandler;
        this.ossDetailHandler = ossDetailHandler;
        this.ossUploaderHandler = ossUploaderHandler;
    }

    /**
     * 文件上传器
     */
    public Map<String, Object> upload(OssUploadRequest request) {
        log.info("文件上传");
        OssIdentifierEntity ossIdentifierEntity = validateOssIdentifier(request.getSource(), request.getSecretId(), request.getSecretKey());

        return uploadFileToLocal(request, ossIdentifierEntity);
    }

    /**
     * 文件上传器
     */
    public void upload(InputStream is, OssUploadInputStreamRequest request) {
        log.info("文件流上传");
        OssIdentifierEntity ossIdentifierEntity = validateOssIdentifier(request.getSource(), request.getSecretId(), request.getSecretKey());

        try {
            // 使用字节数组缓存（适合小文件）
            byte[] fileBytes = IOUtils.toByteArray(is); // 先完整读取流
            String md5 = DigestUtils.md5Hex(fileBytes); // 计算MD5

            String originalFileName = request.getFileName();
            String targetDirectory = getTargetDirectory(request.getSource());
            String targetFilePath = getFilePath(targetDirectory, originalFileName, md5);
            File dest = new File(targetFilePath);

            // 写入文件
            FileParseUtil.writeByteArrayToFile(dest, fileBytes);

            // 构建上传信息
            OssUploadRequest.OssUploadInfo info = new OssUploadRequest.OssUploadInfo();
            info.setMd5(md5);

            // 保存文件及执行触发器
            trigger(info, ossIdentifierEntity, targetFilePath, originalFileName, dest.length());
        } catch (Exception e) {
            log.error("文件流上传失败: {}", request.getFileName(), e);
        }
    }

    /**
     * 删除本地过期文件
     */
    public void deleteExpireLocalFile(){
        List<OssFileEntity> ossFileEntities = ossFileHandler.listExpireOssFiles();
        if (ossFileEntities != null && !ossFileEntities.isEmpty()) {
            for (OssFileEntity ossFileEntity : ossFileEntities) {
                String filePath = ossFileEntity.getFileTempPath();
                File file = new File(filePath);
                if (file.exists() && !file.delete()) {
                    log.error("文件删除失败: {}", filePath);
                }
            }
        }
    }

    public String downloadFile(String filePath) {
        return ossUploaderHandler.downloadFile(System.getProperty("user.home") + File.separator + filePath);
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

    private String checkFileMd5(InputStream is){
        String md5;
        try {
            md5 = FileParseUtil.calculateMD5(is);
        } catch (IOException e) {
            log.error("文件加密异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
        OssFileEntity ossFile = ossFileHandler.getOneByMd5(md5);
        if (ossFile != null) {
            throw new OssException(OssExceptionStatusCode.FILE_EXIST);
        }
        return md5;
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
        List<OssFileEntity> ossFiles = ossFileHandler.listByMd5s(md5s);
        if (ossFiles != null && !ossFiles.isEmpty()) {
            for (OssFileEntity ossUploaderEntity : ossFiles) {
                fileMd5s.add(ossUploaderEntity.getMd5());
            }
        }
        return fileMd5s;
    }

    private OssIdentifierEntity validateOssIdentifier(String source, String secretId, String secretKey){
        OssIdentifierEntity ossIdentifierEntity = ossTriggerHandler.getOssIdentifierBySourceAndAppId(source, AppContext.getAppId());
        if (ossIdentifierEntity == null) {
            throw new OssException(OssExceptionStatusCode.OSS_IDENTIFIER_NOT_FOUND);
        }
        if (!ossIdentifierEntity.getSecretId().equals(secretId) || !ossIdentifierEntity.getSecretKey().equals(secretKey)) {
            throw new OssException(OssExceptionStatusCode.OSS_SECRET_NOT_FOUND);
        }
        return ossIdentifierEntity;
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
        return targetDirectory + File.separator + newFileName + CommonUtil.getFileExt(oldFileName);
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
                    if (ossTriggerEntity.getSeq() == 1) {
                        ossFile = ossFileHandler.saveOssFile(info, ossIdentifier, filePath, fileName, fileSize);
                        fileTrigger.trigger(ossFile, null, ossTriggerEntity);
                    } else {
                        OssDetailEntity ossDetail = ossDetailHandler.saveOssDetail(ossFile, ossTriggerEntity, ossIdentifier.getChannel());
                        OssFileEntity finalOssFile = ossFile;
                        fileUploadTaskExecutor.execute(() -> {
                            fileTrigger.trigger(finalOssFile, ossDetail, ossTriggerEntity);
                        });
                    }
                }
            }
        }
    }
}
