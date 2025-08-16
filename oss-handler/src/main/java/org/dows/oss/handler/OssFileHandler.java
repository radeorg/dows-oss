package org.dows.oss.handler;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssIdentifierEntity;
import org.dows.oss.request.OssUploadRequest;
import org.dows.oss.service.OssDetailService;
import org.dows.oss.service.OssFileService;
import org.dows.oss.utils.CommonUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssFileHandler {

    private final OssFileService ossFileService;
    private final OssDetailService ossDetailService;

    public OssFileEntity saveOssFile(OssUploadRequest.OssUploadInfo info, OssIdentifierEntity ossIdentifierEntity, String filePath, String fileName, Long fileSize){
        OssFileEntity entity = new OssFileEntity();
        entity.setFileName(fileName);
        entity.setMd5(info.getMd5());
        entity.setFileExt(CommonUtil.getFileExt(fileName));
        entity.setFileSize(fileSize);
        entity.setFileTempPath(filePath);
        entity.setBatchNo(CommonUtil.formatDate(new Date(), "yyMMddHH"));
        entity.setAppId(info.getAppId());
        entity.setSource(ossIdentifierEntity.getSource());
        ossFileService.save(entity);
        return entity;
    }

    @Transactional
    public void deleteByMd5AndAppId(String md5, String appId) {
        OssFileEntity entity = ossFileService.getOne(QueryWrapper.create()
                .eq(OssFileEntity::getMd5, md5)
                .eq(OssFileEntity::getAppId, appId));
        if (entity != null){
            ossFileService.removeById(entity.getOssFileId());

            ossDetailService.remove(QueryWrapper.create()
                    .eq(OssDetailEntity::getOssFileId, entity.getOssFileId()));
        }
    }

    public OssFileEntity getByMd5(String md5) {
        return ossFileService.getOne(QueryWrapper.create().eq(OssFileEntity::getMd5, md5));
    }

    public List<OssFileEntity> listByMd5s(List<String> md5s) {
        return ossFileService.list(QueryWrapper.create()
                .in(OssFileEntity::getMd5, md5s));
    }

    public List<OssFileEntity> listExpireOssFiles() {
        LocalDate localDate = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minusDays(1);  // 直接减天数
        return ossFileService.list(QueryWrapper.create().lt(OssFileEntity::getTs, localDate));
    }
}
