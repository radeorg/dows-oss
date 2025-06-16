package org.dows.oss.callback;

import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.response.CallbackResponse;

public interface FileCallback {
    CallbackResponse callback(Object object, OssTriggerEntity ossTriggerEntity);
}

