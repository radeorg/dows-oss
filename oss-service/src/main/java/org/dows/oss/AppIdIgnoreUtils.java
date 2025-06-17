package org.dows.oss;

import com.mybatisflex.core.tenant.TenantManager;

/**
 * @author tangsm
 * @data 2025/5/24 星期六
 * 忽略appId(租户ID）自动填充条件
 */
public class AppIdIgnoreUtils {
    public static void executeWithoutTenant(Runnable task) {
        try {
            TenantManager.ignoreTenantCondition();
            task.run();
        } finally {
            TenantManager.restoreTenantCondition();
        }
    }

//    // 操作的SQL直接通过调用改工具类使用，调用示例：
//    AppIdIgnoreUtils.executeWithoutTenant(() -> {
//        userMapper.deleteById(1L);
//    });
}
