package org.dows.oss.callback;


import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class HttpFileCallback implements FileCallback {
    @Override
    public void callback(Object object, OssTriggerEntity ossTriggerEntity) {
        String triggerTarget = ossTriggerEntity.getCallbackTarget();
        // bean://pkg.class#method,http://url,jdbc://sql...
        try {
            String post = HttpUtil.post(triggerTarget, (Map<String, Object>) null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
