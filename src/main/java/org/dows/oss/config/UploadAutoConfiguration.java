package org.dows.oss.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.dows.oss.mapper")
@ComponentScan(basePackages = {"org.dows.oss.mapper", "org.dows.oss.service",
        "org.dows.oss.config"})

public class UploadAutoConfiguration {
}
