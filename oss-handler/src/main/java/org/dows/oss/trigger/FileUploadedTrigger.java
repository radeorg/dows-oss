package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文件上传成功触发器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FileUploadedTrigger implements FileTrigger{
    @Override
    public void trigger(Object object) {

    }
}
