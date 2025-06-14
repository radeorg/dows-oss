package org.dows.oss.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.rade.oss.S3OssClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssUploader {

    private final Map<String, S3OssClient> ossClientMap;

    /**
     * oss client 上传
     */
    public void upload(Object object) {

        // todo 具体通道上传动作
    }

}
