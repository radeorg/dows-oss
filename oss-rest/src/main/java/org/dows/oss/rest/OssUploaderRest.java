package org.dows.oss.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.FileUploader;
import org.dows.oss.api.FileUploaderApi;
import org.dows.oss.request.OssUploadInputStreamRequest;
import org.dows.oss.request.OssUploadRequest;
import org.dows.rade.oss.OssInfo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OssUploaderRest implements FileUploaderApi {

    private final FileUploader fileUploader;

    @Override
    public OssInfo uploadImg(MultipartFile file) {
        return null;
    }

    @Override
    public String previewImg(String filePath) {
        return "";
    }

    @Override
    public void uploadFile(OssUploadRequest ossUploadRequest) {

    }

    @Override
    public void uploadFile(InputStream is, OssUploadInputStreamRequest request) {

    }

    @Override
    public void callbackTest(String callbackRequest) {

    }
}
