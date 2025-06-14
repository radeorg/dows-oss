package org.dows.oss.callback;

import org.dows.oss.entity.OssTriggerEntity;

public interface FileCallback {
    void callback(Object object, OssTriggerEntity ossTriggerEntity);

}

