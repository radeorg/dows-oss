package org.dows.oss.handler;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.utils.CommonUtil;
import org.dows.rade.oss.OssInfo;
import org.dows.rade.oss.tencent.TencentOssClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssUploaderHandler {

    @Value("${rade.oss.modulePath.uim:/uim}")
    private String orgImgPath;
    private static final List<String> IMG_ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    private final TencentOssClient tencentOssClient;

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
        String savePath = getCosSavePath(request);
        FileInputStream file = new FileInputStream(request.getFileLocalPath());
        if (request.getChannel().equals("COS")) {
            return tencentOssClient.upLoad(new BufferedInputStream(file), savePath, false);
        }
        return null;
    }

    /**
     * 上传markdown/txt解析文件
     */
    public OssInfo uploadFileContent(OssUploadHandlerRequest request, String content){
        if (request.getChannel() != null && request.getChannel().equals("COS")) {
            String savePath = getCosSavePath(request);
            return tencentOssClient.upLoad(new ByteArrayInputStream(content.getBytes()), savePath, false);
        }
        return null;
    }

    public String downloadFile(String filePath) {
        try {
            return getLocalContent(filePath);
        } catch (IOException e) {
            log.error("下载文件失败: {}", filePath, e);
            throw new RuntimeException(e);
        }

    }

    // 获取本地文件内容
    private String getLocalContent(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get(filePath), StandardCharsets.UTF_8)) {
            return readContent(reader);
        }
    }

    // 通用内容读取方法
    private String readContent(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        return content.toString();
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
        if (!IMG_ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
    }

    private String getCosSavePath(OssUploadHandlerRequest request){
        return request.getFilePath() + File.separator
                + CommonUtil.formatDate(new Date(), "yyMMdd") + File.separator
                + request.getMd5() + request.getFileExt();
    }
}
