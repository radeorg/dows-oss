package org.dows.oss.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.FileUploader;
import org.dows.oss.api.FileUploaderApi;
import org.dows.oss.handler.OssUploaderHandler;
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
@Tag(name = "OSS上传接口")
public class OssUploaderRest implements FileUploaderApi {

    private final FileUploader fileUploader;
    private final OssUploaderHandler ossUploaderHandler;

    @Override
    public OssInfo uploadImg(MultipartFile file) {
        return ossUploaderHandler.uploadImgToCos(file);
    }

    @Override
    public String previewImg(String filePath) {
        return ossUploaderHandler.presignedCosViewUrl(filePath);
    }

    @Operation(summary = "上传文件")
    @Override
    public Map<String, Object> uploadFile(OssUploadRequest ossUploadRequest) {
        return fileUploader.upload(ossUploadRequest);
    }

    @Operation(summary = "上传文件")
    @Override
    public void uploadFile(InputStream is, OssUploadInputStreamRequest request) {
        fileUploader.upload(is, request);
    }

    @Operation(summary = "下载文件")
    @Override
    public String downloadFile(String filePath) {
        return fileUploader.downloadFile(filePath);
    }

    @Override
    public void delete(Long ossFileId, String appId) {
        fileUploader.delete(ossFileId, appId);
    }
}
