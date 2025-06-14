package org.dows.oss.api;

import org.springframework.web.bind.annotation.PostMapping;

public interface FileUploaderApi {

    @PostMapping("/v1/oss/file/upload")
    void upload(Object object);
}
