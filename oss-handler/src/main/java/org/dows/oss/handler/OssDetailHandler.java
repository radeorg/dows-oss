package org.dows.oss.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.oss.request.OssUploadTriggerCallbackRequest;
import org.dows.oss.service.OssDetailService;
import org.dows.rade.oss.OssInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssDetailHandler {

    private final Map<String, FileCallback> fileCallbackMap;
    private final OssDetailService ossDetailService;

    public OssUploadHandlerRequest toOssUploadHandlerRequest(OssFileEntity ossFile, OssDetailEntity ossDetail){
        OssUploadHandlerRequest request = new OssUploadHandlerRequest();
        request.setMd5(ossFile.getMd5());
        request.setFileExt(ossFile.getFileExt());
        request.setFileLocalPath(ossFile.getFileTempPath());
        request.setFilePath(ossDetail.getBasePath());
        request.setChannel(ossDetail.getChannel());
        return request;
    }

    public void updateOssDetail(OssInfo info, OssDetailEntity ossDetail){
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

    public void callback(OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity){
        String callbackTarget = ossTriggerEntity.getCallbackTarget();
        if (!callbackTarget.isEmpty()) {
            String[] split = callbackTarget.split(":");

            FileCallback fileCallback = fileCallbackMap.get(split[0] + "FileCallback");
            CallbackResponse callbackResponse = fileCallback.callback(toOssUploadTriggerResponse(ossDetail), ossTriggerEntity);
            if (callbackResponse.getSuccess()){
                ossDetail.setState(1);
            } else {
                ossDetail.setState(2);
                ossDetail.setFailedReason(callbackResponse.getMessage());
            }
            ossDetailService.updateById(ossDetail);
        }
    }

    private OssUploadTriggerCallbackRequest toOssUploadTriggerResponse(OssDetailEntity ossDetail){
        OssUploadTriggerCallbackRequest response = new OssUploadTriggerCallbackRequest();
        response.setMd5(ossDetail.getMd5());
        response.setOssFileId(ossDetail.getOssFileId());
        response.setOssDetailId(ossDetail.getOssDetailId());
        response.setAppId(ossDetail.getAppId());
        response.setTrigger(ossDetail.getTrigger());
        response.setFilePath(ossDetail.getBasePath());
        response.setFileExt(ossDetail.getFileExt());
        response.setFileLink(ossDetail.getFileLink());
        response.setFileSize(ossDetail.getFileSize());
        response.setStoreType(ossDetail.getChannel());
        return response;
    }
}
