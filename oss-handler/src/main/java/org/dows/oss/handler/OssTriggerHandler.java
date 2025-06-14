package org.dows.oss.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.service.OssIdentifierService;
import org.dows.oss.service.OssTriggerService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssTriggerHandler {

    private final OssIdentifierService ossIdentifierService;
    private final OssTriggerService ossTriggerService;


    //根据secretId和secretKey获取用户配置触发器
    @Cacheable(value = "triggerListCache", key = "'secretId:' + #secretId + 'secretKey:' + #secretKey")
    public List<OssTriggerEntity> triggerList(String secretId, String secretKey) {

        return null;
        //return ossTriggerService.list(new OssTriggerEntity().setOssIdentifierId(secretId));
    }
}
