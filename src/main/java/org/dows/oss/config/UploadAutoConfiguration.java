package org.dows.oss.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.dows.oss.mapper")
@ComponentScan(basePackages = {"org.dows.oss.mapper", "org.dows.oss.service", "org.dows.oss.biz",
        "org.dows.oss.config", "org.dows.oss.scheduler", "org.dows.oss.listener", "org.dows.oss.handler"})

public class UploadAutoConfiguration {
}
