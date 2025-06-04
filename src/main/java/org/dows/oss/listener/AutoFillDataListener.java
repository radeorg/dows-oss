package org.dows.oss.listener;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import org.dows.rade.aac.AacUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * @author tangsm
 * @data 2025/5/22 星期四
 * 新增时自动填充创建时间和操作人，修改时自动填充更新时间
 */
public class AutoFillDataListener implements InsertListener, UpdateListener {
    @Override
    public void onInsert(Object entity) {
        Date date = new Date(); // 创建时间和更新时间使用同一参数值，避免执行过程中有些延迟导致不一致
        insertDate(entity, date, "ts");
        insertDate(entity, date, "ut");
        insertOperatorId(entity);
    }

    @Override
    public void onUpdate(Object entity) {
        insertDate(entity, new Date(), "ut");
    }

    private void insertDate(Object entity, Date date, String filedName) {
        try {
            // 通过反射检测并设置更新时间字段
            Field updateTimeField = entity.getClass().getDeclaredField(filedName);
            if (updateTimeField.getType() == Date.class) {
                updateTimeField.setAccessible(true);
                updateTimeField.set(entity, date);
            }
        } catch (Exception e) {
            // 忽略无更新时间字段的实体类
            e.printStackTrace();
        }
    }

    private void insertOperatorId(Object entity) {
        try {
            Field operatorIdField = entity.getClass().getDeclaredField("operatorId");
            if (operatorIdField.getType() == Long.class) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Object principal = authentication.getPrincipal();
                if (principal instanceof AacUser aacUser) {
                    operatorIdField.setAccessible(true);
                    operatorIdField.set(entity, aacUser.getAccountId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}