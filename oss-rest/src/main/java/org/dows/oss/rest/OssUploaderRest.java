package org.dows.oss.rest;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.FileUploader;
import org.dows.oss.api.FileUploaderApi;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OssUploaderRest implements FileUploaderApi {

    private final FileUploader fileUploader;

    @Override
    public void upload(Object object) {
        fileUploader.upload(object);
    }
}
