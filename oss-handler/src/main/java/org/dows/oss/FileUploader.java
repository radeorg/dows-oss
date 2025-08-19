package org.dows.oss;

import com.qcloud.cos.utils.IOUtils;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.dows.oss.constant.OssExceptionStatusCode;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssIdentifierEntity;
import org.dows.oss.handler.*;
import org.dows.oss.request.OssUploadInputStreamRequest;
import org.dows.oss.request.OssUploadRequest;
import org.dows.oss.utils.CommonUtil;
import org.dows.oss.utils.FileParseUtil;
import org.dows.rade.context.AppContext;
import org.dows.rade.oss.OssException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
@RequiredArgsConstructor
public class FileUploader {

    private final OssDetailHandler ossDetailHandler;
    @Value("${rade.oss.path:/radeorg}")
    private String orgPath;
    private static final List<String> SUPPORTED_FILE_EXTENSIONS = List.of(".doc", ".docx", ".pdf");

    private final OssTriggerHandler ossTriggerHandler;
    private final OssFileHandler ossFileHandler;
    private final OssUploaderHandler ossUploaderHandler;
    private final FileUploaderTriggerHandler fileUploaderTriggerHandler;

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
    @Transactional
    public void upload(InputStream is, OssUploadInputStreamRequest request) {
        log.info("文件流上传");
        OssIdentifierEntity ossIdentifierEntity = validateOssIdentifier(request.getSource(), request.getSecretId(), request.getSecretKey());

        try {
            // 使用字节数组缓存（适合小文件）
            byte[] fileBytes = IOUtils.toByteArray(is); // 先完整读取流
            String md5 = DigestUtils.md5Hex(fileBytes); // 计算MD5

            // 构建上传信息
            OssUploadRequest.OssUploadInfo info = new OssUploadRequest.OssUploadInfo();
            info.setMd5(md5);
            info.setAppId(request.getAppId());

            OssFileEntity ossFile = ossFileHandler.getByMd5(md5);
            if (ossFile != null) {
                fileUploaderTriggerHandler.repeatTrigger(info, ossIdentifierEntity, ossFile, request.getFileName());
            } else {
                String originalFileName = request.getFileName();
                String targetDirectory = getTargetDirectory(request.getSource());
                String targetFilePath = getFilePath(targetDirectory, originalFileName, md5);
                File dest = new File(targetFilePath);

                // 写入文件
                FileParseUtil.writeByteArrayToFile(dest, fileBytes);

                // 保存文件及执行触发器
                fileUploaderTriggerHandler.trigger(info, ossIdentifierEntity, targetFilePath, originalFileName, dest.length());
            }
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
        return ossUploaderHandler.downloadFile(filePath);
    }

    public void delete(String md5, String appId) {
        ossFileHandler.deleteByMd5AndAppId(md5, appId);
    }

    /**
     * 上传文件到服务器
     */
    private Map<String, Object> uploadFileToLocal(OssUploadRequest ossUploadRequest, OssIdentifierEntity ossIdentifierEntity){
        int successNum = 0;
        Map<String, String> failInfo = new HashMap<>();
        List<String> distinctMd5s = new ArrayList<>();
        Map<String, OssFileEntity> existUploaderFileMd5 = queryExistUploaderFileMd5(ossUploadRequest);
        for (OssUploadRequest.OssUploadInfo info : ossUploadRequest.getInfos()) {
            String md5 = info.getMd5();
            if (!distinctMd5s.contains(md5)) {
                // 同一份文件在云服务器只存在一份，但是可以有多条企业上传记录
                if (existUploaderFileMd5.containsKey(md5)) {
                    OssFileEntity ossFile = existUploaderFileMd5.get(md5);
                    if (ossFile.getAppId().equals(AppContext.getAppId())) {
                        failInfo.put(info.getMd5(), "文件重复");
                    } else {
                        // 执行触发器
                        info.setIsExist(true);
                        String fileName = getFileName(info.getFile());
                        fileUploaderTriggerHandler.repeatTrigger(info, ossIdentifierEntity, ossFile, fileName);

                        successNum++;
                    }

                    distinctMd5s.add(md5);
                } else {
                    String fileName = getFileName(info.getFile());
                    if (!SUPPORTED_FILE_EXTENSIONS.contains(CommonUtil.getFileExt(fileName))) {
                        failInfo.put(info.getMd5(), "暂不支持该类型文件上传");
                    } else {
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
                            fileUploaderTriggerHandler.trigger(info, ossIdentifierEntity, filePath, fileName, dest.length());

                            successNum++;
                        } catch (Exception e) {
                            log.error("文件上传失败: {}", fileName, e);
                            if (dest.exists() && !dest.delete()) {
                                log.error("文件删除失败: {}", dest.getAbsolutePath());
                            }
                            failInfo.put(info.getMd5(), e.getMessage());
                        }
                    }
                }
            } else {
                failInfo.put(info.getMd5(), "文件重复");
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
    private Map<String, OssFileEntity> queryExistUploaderFileMd5(OssUploadRequest ossUploadRequest){
        // 过滤出不重复、不为空的md5集合
        List<String> md5s = ossUploadRequest.getInfos().stream()
                .map(OssUploadRequest.OssUploadInfo::getMd5)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, OssFileEntity> fileMd5s = new HashMap<>();
        List<OssFileEntity> ossFiles = ossFileHandler.listByMd5s(md5s);
        if (ossFiles != null && !ossFiles.isEmpty()) {
            for (OssFileEntity ossUploaderEntity : ossFiles) {
                fileMd5s.put(ossUploaderEntity.getMd5(), ossUploaderEntity);
            }
        }
        return fileMd5s;
    }

    private OssIdentifierEntity validateOssIdentifier(String source, String secretId, String secretKey){
        OssIdentifierEntity ossIdentifierEntity = ossTriggerHandler.getOssIdentifierBySource(source);
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
}
