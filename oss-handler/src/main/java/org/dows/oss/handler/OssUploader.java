package org.dows.oss.handler;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.utils.CommonUtil;
import org.dows.oss.utils.FileParseUtil;
import org.dows.rade.oss.OssInfo;
import org.dows.rade.oss.S3OssClient;
import org.dows.rade.oss.tencent.TencentOssClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssUploader {

    @Value("${rade.oss.modulePath.uim:/uim}")
    private String orgImgPath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    private final TencentOssClient tencentOssClient;
    private final Map<String, S3OssClient> ossClientMap;

    /**
     * 上传文件到COS服务
     */
    public OssInfo uploadImgToCos(MultipartFile file)  {
        String fileName = getFileName(file);
        checkFileExtension(fileName);
        String savePath = String.format("%s/%s.%s", orgImgPath, UUID.randomUUID(), fileName.substring(fileName.lastIndexOf(".")+1));
        try {
            return tencentOssClient.upLoad(file.getInputStream(), savePath, false);
        } catch (IOException e) {
            log.error("上传文件失败: {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取COS文件临时访问地址
     */
    public String presignedCosViewUrl(String filePath){
        return tencentOssClient.presignedViewUrl(filePath, 5 * 60L);
    }

    /**
     * 上传原始文件
     */
    public OssInfo uploadOriginalFile(OssUploadHandlerRequest request) throws FileNotFoundException {
        String savePath = request.getFilePath() + File.separator
                + CommonUtil.formatDate(new Date(), "yyMMdd") + File.separator
                + request.getMd5()
                + request.getFileExt();
        FileInputStream file = new FileInputStream(request.getFileLocalPath());
        if (request.getChannel().equals("COS")) {
            return tencentOssClient.upLoad(new BufferedInputStream(file), savePath, false);
        }
        return null;
    }

    /**
     * 上传markdown解析文件
     */
    public OssInfo uploadParseMarkDownFile(OssUploadHandlerRequest request) throws IOException {
        String savePath = request.getFilePath() + File.separator
                + CommonUtil.formatDate(new Date(), "yyMMdd") + File.separator
                + request.getMd5() + ".md";
        String parseContent = FileParseUtil.convertToMarkdown(request.getFileLocalPath());
        if (request.getChannel().equals("COS")) {
            return tencentOssClient.upLoad(new ByteArrayInputStream(parseContent.getBytes()), savePath, false);
        }
        return null;
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

    private void checkFileExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
    }

    // 获取文件文本内容
    public String downContent(String filePath,Long ossDetailId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 通过OSS客户端下载文件内容到内存
            tencentOssClient.downLoad(baos, filePath);
            return baos.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("文件内容获取失败: " + filePath, e);
        }
    }
}
