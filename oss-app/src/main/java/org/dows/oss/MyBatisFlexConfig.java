package org.dows.oss;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.rade.crud.AutoFillDataListener;
import org.dows.rade.crud.BaseEntity;
import org.dows.rade.crud.FieldFillListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class MyBatisFlexConfig {

//    private final AppIdContext aacContext;

    @Value("${rade.log.sql.printSql:false}")
    private boolean printSql;

    @PostConstruct
    public void init() {
        if (printSql) {
            // 开启审计功能
            AuditManager.setAuditEnable(true);
            // 设置 SQL 审计收集器
            AuditManager.setMessageCollector(auditMessage ->
                    log.info("{},{}ms", auditMessage.getFullSql(), auditMessage.getElapsedTime())
            );
        }
        // 获取全局配置
        FlexGlobalConfig defaultConfig = FlexGlobalConfig.getDefaultConfig();
        // 为所有 BaseEntity 的子类添加监听器
        FieldFillListener fieldFillListener = new FieldFillListener();
        defaultConfig.registerInsertListener(fieldFillListener, BaseEntity.class);
        defaultConfig.registerUpdateListener(fieldFillListener, BaseEntity.class);
    }

    @Bean
    public MyBatisFlexCustomizer flexCustomizer() {
        return flex -> flex.registerUpdateListener(new AutoFillDataListener());
    }
}
