package org.dows.oss.api;

import org.dows.oss.request.OssUploadInputStreamRequest;
import org.dows.oss.request.OssUploadRequest;
import org.dows.rade.oss.OssInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

public interface FileUploaderApi {

    /**
     * 上传图片至COS
     * @param file 上传的图片文件
     * @return 图片对象
     */
    @PostMapping("/v1/open/oss/img/upload")
    OssInfo uploadImg(MultipartFile file);

    /**
     * 根据图片路径获取临时图片预览地址
     * @param filePath 图片路径
     * @return 临时图片预览地址
     */
    @GetMapping("/v1/open/oss/img/preview")
    String previewImg(@RequestParam String filePath);

    /**
     * 上传文件至本地服务器（支持图片、文档）
     * @param ossUploadRequest 业务系统请求的参数
     */
    @PostMapping(value = "/v1/open/oss/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> uploadFile(@ModelAttribute OssUploadRequest ossUploadRequest);

    /**
     * 文件流形式上传文件
     */
    void uploadFile(InputStream is, OssUploadInputStreamRequest request);
}
