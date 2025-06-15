package org.dows.oss.trigger;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.springframework.stereotype.Component;

/**
 * 文件转换触发器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MdTransformTrigger implements FileTrigger {

    @Override
    public void trigger(Long ossFileId, Object object, OssTriggerEntity ossTriggerEntity) {

    }
}
