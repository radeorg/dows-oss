package org.dows.oss;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 * @data 2025/6/16 星期一
 */
@Configuration
@MapperScan("org.dows.oss.mapper")
@ComponentScan(basePackages = {"org.dows.oss.mapper", "org.dows.oss.service"})
public class UploadAutoConfiguration {
}

