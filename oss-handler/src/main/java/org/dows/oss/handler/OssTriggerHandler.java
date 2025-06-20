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
     * 根据secretId和secretKey获取用户配置触发器（过期时间5分钟）
     */
    @Cacheable(value = "ossIdentifierCache#3000", key = "'source:' + #source + 'appId:' + #appId")
    public OssIdentifierEntity getOssIdentifierBySourceAndAppId(String source, String appId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(OssIdentifierEntity::getSource, source)
                .eq(OssIdentifierEntity::getAppId, appId);
        return ossIdentifierService.getOne(queryWrapper);
    }

    /**
     * 根据ossIdentifierId获oss取用户配置触发器（过期时间5分钟）
     */
    @Cacheable(value = "triggerListCache#3000", key = "'ossIdentifierId:' + #ossIdentifierId")
    public List<OssTriggerEntity> triggerList(Long ossIdentifierId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(OssTriggerEntity::getOssIdentifierId, ossIdentifierId)
                .orderBy(OssTriggerEntity::getSeq, true);
        return ossTriggerService.list(queryWrapper);
    }
}
