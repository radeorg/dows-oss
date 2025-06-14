package org.dows.oss.callback;

import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeanFileCallback implements FileCallback {
    @Override
    public void callback(Object object, OssTriggerEntity ossTriggerEntity) {

        String triggerTarget = ossTriggerEntity.getCallbackTarget();
        // bean://pkg.class#method,http://url,jdbc://sql...
        String[] split = triggerTarget.split("");

        Object bean = SpringUtil.getBean(split[1]);

        try {
            Method method = bean.getClass().getMethod(split[2]);

            Object result = method.invoke(bean);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
