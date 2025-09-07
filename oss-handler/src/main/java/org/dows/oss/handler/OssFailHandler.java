package org.dows.oss.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.service.OssFailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssFailHandler {

    private final OssFailService ossFailService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOssFail(OssFileEntity ossFile, OssDetailEntity ossDetail,
                            OssTriggerEntity trigger, String failReason) {
        OssFailEntity failEntity = new OssFailEntity();
        failEntity.setMd5(ossFile.getMd5());
        failEntity.setFileName(ossFile.getFileName());
        failEntity.setSource(ossFile.getSource());
        failEntity.setFileExt(ossFile.getFileExt());
        failEntity.setFilePath(ossDetail.getFilePath());
        failEntity.setFileLink(ossDetail.getFileLink());
        failEntity.setChannel(ossDetail.getChannel());
        failEntity.setTrigger(trigger.getTrigger());
        failEntity.setFailedReason(failReason);
        failEntity.setAppId(ossFile.getAppId());
        ossFailService.save(failEntity);
    }
}
