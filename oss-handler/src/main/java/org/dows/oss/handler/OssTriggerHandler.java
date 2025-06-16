package org.dows.oss.handler;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssIdentifierEntity;
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

    /**
     * 根据secretId和secretKey获取用户配置触发器
     */
    @Cacheable(value = "ossIdentifierCache", key = "'source:' + #source + 'appId:' + #appId")
    public OssIdentifierEntity getOssIdentifierBySourceAndAppId(String source, String appId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(OssIdentifierEntity::getSource, source)
                .eq(OssIdentifierEntity::getAppId, appId);
        return ossIdentifierService.getOne(queryWrapper);
    }

    /**
     * 根据ossIdentifierId获oss取用户配置触发器
     */
    @Cacheable(value = "triggerListCache", key = "'ossIdentifierId:' + #ossIdentifierId")
    public List<OssTriggerEntity> triggerList(Long ossIdentifierId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(OssTriggerEntity::getOssTriggerId, ossIdentifierId);
        return ossTriggerService.list(queryWrapper);
    }
}
