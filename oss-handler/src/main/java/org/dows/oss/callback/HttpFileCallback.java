package org.dows.oss.callback;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.response.CallbackResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class HttpFileCallback implements FileCallback {
    @Override
    public CallbackResponse callback(Object object, OssTriggerEntity ossTriggerEntity) {
        CallbackResponse response = new CallbackResponse();
        String triggerTarget = ossTriggerEntity.getCallbackTarget();
        try {
            ResponseEntity<String> callbackResponse = new RestTemplate().postForEntity(triggerTarget, object, String.class);
            if (callbackResponse.getStatusCode().toString().equals("200 OK")) {
                response.setSuccess(true);
            } else {
                response.setSuccess(false);
                response.setMessage(callbackResponse.getStatusCode().toString());
            }
        } catch (Exception e) {
            log.error("BeanFileCallback callback error", e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            throw new OssFileException(e.getMessage());
        }
        return response;
    }
}
