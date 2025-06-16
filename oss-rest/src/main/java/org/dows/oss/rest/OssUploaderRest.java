package org.dows.oss.rest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.FileUploader;
import org.dows.oss.api.FileUploaderApi;
import org.dows.oss.handler.OssUploader;
import org.dows.oss.request.OssUploadInputStreamRequest;
import org.dows.oss.request.OssUploadRequest;
import org.dows.rade.oss.OssInfo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OssUploaderRest implements FileUploaderApi {

    private final FileUploader fileUploader;
    private final OssUploader ossUploader;

    @Override
    public OssInfo uploadImg(MultipartFile file) {
        return ossUploader.uploadImgToCos(file);
    }

    @Override
    public String previewImg(String filePath) {
        return ossUploader.presignedCosViewUrl(filePath);
    }

    @Override
    public Map<String, Object> uploadFile(OssUploadRequest ossUploadRequest) {
        return fileUploader.upload(ossUploadRequest);
    }

    @Override
    public void uploadFile(InputStream is, OssUploadInputStreamRequest request) {
        fileUploader.upload(is, request);
    }

    /**
     * 下载文件解析内容
     * @param ossFilePath ossDetailId 业务系统请求的参数
     */
    public String downContent( String ossFilePath,Long ossDetailId){
        return ossUploader.downContent(ossFilePath,ossDetailId);
    }



    @Override
    public void callbackTest(String callbackRequest) {

    }
}
