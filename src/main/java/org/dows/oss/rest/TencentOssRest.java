package org.dows.oss.rest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.api.TencentOssApi;
import org.dows.oss.biz.OssFileBiz;
import org.dows.rade.oss.OssInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author tangsm
 * @data 2025/5/20 星期二
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TencentOssRest implements TencentOssApi {

    private final OssFileBiz ossFileBiz;

    @Operation(summary = "上传图片至COS")
    public OssInfo uploadImg(MultipartFile file) {
        return ossFileBiz.uploadImgToCos(file);
    }

    @Operation(summary = "根据图片路径获取COS临时图片预览地址")
    public String uploadImg(String filePath) {
        return ossFileBiz.presignedViewUrl(filePath);
    }

    @Operation(summary = "上传文件至本地服务器（支持图片、文档）")
    public Map<String, Object> uploads(MultipartFile[] files, String ossUploadRequest) {
        return ossFileBiz.uploadFileToLocal(files, ossUploadRequest);
    }

    /**
     * 下载文件解析内容
     * @param ossFilePath 业务系统请求的参数
     */

    @Operation(summary = "下载文件解析内容")
    public String downContent( String ossFilePath,Long ossDetailId){
        return ossFileBiz.downContent(ossFilePath,ossDetailId);
    }

    @Operation(summary = "回调测试")
    public void callbackTest(String callbackRequest) {
        System.out.println("回调成功：" + callbackRequest);
    }
}
