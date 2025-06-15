package org.dows.oss.api;

import org.dows.rade.oss.OssInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author tangsm
 * @data 2025/6/3 星期二
 */
public interface TencentOssApi {

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
    String uploadImg(@RequestParam String filePath);

    /**
     * 上传文件至本地服务器（支持图片、文档）
     * @param files 上传的文件
     * @param ossUploadRequest 业务系统请求的参数
     */
    @PostMapping("/v1/open/oss/uploads")
    Map<String, Object> uploads(MultipartFile[] files, @RequestParam String ossUploadRequest);

    /**
     * 下载文件解析内容
     * @param ossFilePath 业务系统请求的参数
     */
    @PostMapping("/v1/open/oss/downContent")
    String downContent( @RequestParam String ossFilePath,@RequestParam Long ossDetailId);


    /**
     * 回调测试接口
     */
    @PostMapping("/v1/open/oss/test/callback")
    void callbackTest(@RequestParam String callbackRequest,@RequestParam Long ossDetailId);
}
