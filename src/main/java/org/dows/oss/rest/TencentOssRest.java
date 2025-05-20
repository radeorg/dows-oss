package org.dows.oss.rest;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.rade.oss.OssInfo;
import org.dows.rade.oss.tencent.TencentOssClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @data 2025/5/20 星期二
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TencentOssRest {
    @Value("${rade.oss.modulePath.uim:/uim}")
    private String orgImgPath;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private final TencentOssClient tencentOssClient;

    @PostMapping("/v1/open/oss/img/upload")
    public String uploadImg(MultipartFile file) {
        String fileName = getFileName(file);
        checkFileExtension(fileName);
        String savePath = orgImgPath + String.format("%s.jpg", File.separator + System.currentTimeMillis());
        try {
            OssInfo ossInfo = tencentOssClient.upLoad(file.getInputStream(), savePath, false);
            return ossInfo.getFilePath();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

    public static String formatDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
