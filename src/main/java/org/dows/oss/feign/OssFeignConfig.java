package org.dows.oss.feign;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Slf4j
@Configuration
@EnableFeignClients(basePackages={"org.dows.oss.feign","org.dows.oss.api"})
@EnableRetry
public class OssFeignConfig {

    @PostConstruct
    public void init(){
        log.info("init");
    }
}
