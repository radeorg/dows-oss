package org.dows.oss.handler;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.service.OssFileService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssFileHandler {

    private final OssFileService ossFileService;

    public OssFileEntity saveOssFile(OssFileEntity entity) {
        ossFileService.save(entity);
        return entity;
    }

    public OssFileEntity getOneByMd5(String md5) {
        return ossFileService.getOne(QueryWrapper.create().eq(OssFileEntity::getMd5, md5));
    }

    public List<OssFileEntity> listByMd5s(List<String> md5s) {
        return ossFileService.list(QueryWrapper.create().in(OssFileEntity::getMd5, md5s));
    }

    public List<OssFileEntity> listExpireOssFiles() {
        LocalDate localDate = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minusDays(1);  // 直接减天数
        return ossFileService.list(QueryWrapper.create().lt(OssFileEntity::getTs, localDate));
    }
}
