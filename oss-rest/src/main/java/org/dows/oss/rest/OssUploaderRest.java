package org.dows.oss.rest;

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

    @Override
    public Map<String, Object> uploadFile(OssUploadRequest ossUploadRequest) {
        return fileUploader.upload(ossUploadRequest);
    }

    @Override
    public void uploadFile(InputStream is, OssUploadInputStreamRequest request) {
        fileUploader.upload(is, request);
    }
}
