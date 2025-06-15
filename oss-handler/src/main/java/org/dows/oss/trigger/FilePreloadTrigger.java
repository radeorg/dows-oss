package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.handler.OssUploader;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文件预加载触发
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FilePreloadTrigger {

    private final Map<String, FileCallback> fileCallbackMap;

    private final OssUploader ossUploader;


    public Long trigger(Object object, OssTriggerEntity ossTriggerEntity) {

        // todo 存oss_file 表
        // todo call_back

        return null;

    }
}
