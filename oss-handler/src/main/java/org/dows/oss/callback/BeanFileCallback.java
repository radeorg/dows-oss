package org.dows.oss.callback;

import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.response.CallbackResponse;
import org.dows.rade.web.Response;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

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
            throw new OssFileException(e.getMessage());
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

    private Object invokeBeanMethod(String beanName, String methodName, Object... args) {
        Object bean = SpringUtil.getBean(beanName);
        Objects.requireNonNull(bean, "Bean not found: " + beanName);

        try {
            Class<?>[] paramTypes = args != null ?
                    Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new) : new Class<?>[0];

            Method method = ReflectionUtils.findMethod(
                    AopUtils.getTargetClass(bean), methodName, paramTypes);
            Objects.requireNonNull(method, "Method not found: " + methodName);

            ReflectionUtils.makeAccessible(method);
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            Throwable targetEx = e.getTargetException();
            throw (targetEx instanceof RuntimeException) ?
                    (RuntimeException)targetEx : new RuntimeException(targetEx);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Method access denied", e);
        }
    }
}
