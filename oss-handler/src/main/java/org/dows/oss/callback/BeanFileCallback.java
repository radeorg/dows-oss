package org.dows.oss.callback;

import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.response.CallbackResponse;
import org.dows.rade.web.Response;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeanFileCallback implements FileCallback {
    @Override
    public CallbackResponse callback(Object object, OssTriggerEntity ossTriggerEntity) {
        CallbackResponse response = new CallbackResponse();
        try {
            String triggerTarget = ossTriggerEntity.getCallbackTarget();
            String beanName = extractBean(triggerTarget);
            String methodName = extractMethod(triggerTarget);

            Object obj = invokeBeanMethod(beanName, methodName, object);
            Response res = (Response) obj;
            if (res.getCode().equals("200")) {
                response.setSuccess(true);
            } else {
                response.setSuccess(false);
                response.setMessage(res.getDescription());
            }
        } catch (Exception e) {
            log.error("BeanFileCallback callback error:{}", e.getMessage());
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    private String extractBean(String input) {
        int start = input.indexOf("//") + 2;
        int end = input.indexOf("#");
        return input.substring(start, end);
    }

    private String extractMethod(String input) {
        return input.substring(input.indexOf("#") + 1);
    }

    private Object invokeBeanMethod(String beanName, String methodName, Object arg) {
        Object bean = SpringUtil.getBean(beanName);
        try {
            // 遍历所有方法匹配名称和注解
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    Parameter[] params = method.getParameters();
                    if (params.length == 1 &&
                            params[0].isAnnotationPresent(RequestBody.class)) {
                        return method.invoke(bean, arg);
                    }
                }
            }
            throw new NoSuchMethodException(methodName);
        } catch (Exception e) {
            throw new RuntimeException("Invocation failed", e);
        }
    }
}
