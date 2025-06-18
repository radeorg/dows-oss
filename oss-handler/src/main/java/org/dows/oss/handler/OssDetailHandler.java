package org.dows.oss.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.constant.PatternConstant;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.oss.request.OssUploadTriggerCallbackRequest;
import org.dows.oss.service.OssDetailService;
import org.dows.oss.utils.CommonUtil;
import org.dows.rade.oss.OssInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssDetailHandler {

    private final Map<String, FileCallback> fileCallbackMap;
    private final OssDetailService ossDetailService;

    public CallbackResponse callback(OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity){
        return callback(ossDetail, ossTriggerEntity, null);
    }

    public CallbackResponse callback(OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity, String content){
        String callbackTarget = ossTriggerEntity.getCallbackTarget();
        if (!callbackTarget.isEmpty()) {
            String[] split = callbackTarget.split(":");
            if (split.length > 0) {
                String phone = CommonUtil.extractPattern(content, PatternConstant.PHONE_PATTERN);
                String email = CommonUtil.extractPattern(content, PatternConstant.EMAIL_PATTERN);
                OssUploadTriggerCallbackRequest request = toTriggerCallbackRequest(ossDetail, phone, email);

                FileCallback fileCallback = fileCallbackMap.get(split[0] + "FileCallback");
                return fileCallback.callback(request, ossTriggerEntity);
            }
        }
        return null;
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
                ossDetail.setState(2);
                ossDetail.setFailedReason(callbackResponse.getMessage());
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

    private OssUploadTriggerCallbackRequest toTriggerCallbackRequest(OssDetailEntity ossDetail, String phone, String email){
        OssUploadTriggerCallbackRequest request = new OssUploadTriggerCallbackRequest();
        request.setMd5(ossDetail.getMd5());
        request.setOssFileId(ossDetail.getOssFileId());
        request.setOssDetailId(ossDetail.getOssDetailId());
        request.setAppId(ossDetail.getAppId());
        request.setTrigger(ossDetail.getTrigger());
        request.setFilePath(ossDetail.getBasePath());
        request.setFileExt(ossDetail.getFileExt());
        request.setFileLink(ossDetail.getFileLink());
        request.setFileSize(ossDetail.getFileSize());
        request.setStoreType(ossDetail.getChannel());
        request.setPhone(phone);
        request.setEmail(email);
        return request;
    }
}
