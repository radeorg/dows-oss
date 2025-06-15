package org.dows.oss.trigger;

import org.dows.oss.entity.OssTriggerEntity;

public interface FileTrigger/* extends Runnable*/{

    void trigger(Long ossFileId, Object object, OssTriggerEntity ossTriggerEntity);


}
