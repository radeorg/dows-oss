package org.dows.oss.handler;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.oss.service.OssDetailService;
import org.dows.rade.oss.OssInfo;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssDetailHandler {

    private final OssDetailService ossDetailService;

    public OssDetailEntity saveOssDetail(OssFileEntity ossFile, OssTriggerEntity trigger, String channel) {
        OssDetailEntity detailEntity = new OssDetailEntity();
        detailEntity.setOssFileId(ossFile.getOssFileId());
        detailEntity.setAppId(ossFile.getAppId());
        detailEntity.setTrigger(trigger.getTrigger());
        detailEntity.setChannel(channel);
        detailEntity.setBasePath(trigger.getBasePath());
        detailEntity.setSeq(trigger.getSeq());
        detailEntity.setRetryCount(trigger.getRetryCount());
        ossDetailService.save(detailEntity);
        return detailEntity;
    }

    public OssDetailEntity saveOssDetail(OssFileEntity oldFile, OssFileEntity ossFile, OssTriggerEntity trigger, String channel) {
        OssDetailEntity detailEntity = new OssDetailEntity();
        detailEntity.setOssFileId(ossFile.getOssFileId());
        detailEntity.setAppId(ossFile.getAppId());
        detailEntity.setTrigger(trigger.getTrigger());
        detailEntity.setChannel(channel);
        detailEntity.setBasePath(trigger.getBasePath());
        detailEntity.setSeq(trigger.getSeq());
        detailEntity.setRetryCount(trigger.getRetryCount());

        OssDetailEntity detail = getByOldFileIdAndTrigger(oldFile, trigger.getTrigger());
        if (detail != null) {
            detailEntity.setMd5(detail.getMd5());
            detailEntity.setFileSize(detail.getFileSize());
            detailEntity.setFileName(detail.getFileName());
            detailEntity.setFilePath(detail.getFilePath());
            detailEntity.setFileLink(detail.getFileLink());
            detailEntity.setFileExt(detail.getFileExt());
        }
        ossDetailService.save(detailEntity);
        return detailEntity;
    }

    public void updateOssDetailFileInfo(OssInfo info, OssDetailEntity ossDetail){
        if (info != null) {
            ossDetail.setMd5(info.getMd5());
            ossDetail.setFileName(info.getName());
            ossDetail.setFileLink(info.getFileLink());
            ossDetail.setFilePath(info.getFilePath());
            ossDetail.setFileSize(Long.valueOf(info.getSize()));
            ossDetail.setFileExt(info.getName().substring(info.getName().lastIndexOf(".")));
            ossDetailService.updateById(ossDetail);
        }
    }

    public void updateOssDetailCallbackInfo(OssDetailEntity ossDetail, CallbackResponse callbackResponse){
        if (callbackResponse != null) {
            if (callbackResponse.getSuccess()){
                ossDetail.setState(1);
            } else {
                throw new OssFileException(callbackResponse.getMessage());
            }
            ossDetailService.updateById(ossDetail);
        }
    }

    public OssUploadHandlerRequest toOssUploadHandlerRequest(OssFileEntity ossFile, OssDetailEntity ossDetail){
        OssUploadHandlerRequest request = new OssUploadHandlerRequest();
        request.setMd5(ossFile.getMd5());
        request.setFileExt(ossFile.getFileExt());
        request.setFileLocalPath(ossFile.getFileTempPath());
        request.setFilePath(ossDetail.getBasePath());
        request.setChannel(ossDetail.getChannel());
        return request;
    }

    public OssDetailEntity getByOldFileIdAndTrigger(OssFileEntity oldFile, String trigger) {
        return ossDetailService.getOne(QueryWrapper.create()
                .eq(OssDetailEntity::getOssFileId, oldFile.getOssFileId())
                .eq(OssDetailEntity::getAppId, oldFile.getAppId())
                .eq(OssDetailEntity::getTrigger, trigger)
        );
    }
}
