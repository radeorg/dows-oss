package org.dows.oss.trigger;

import cn.hutool.core.util.StrUtil;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.request.OssUploadCallbackRequest;
import org.dows.oss.request.OssUploadTriggerCallbackRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.oss.util.ExtractUtil;
import org.dows.rade.util.SpringUtil;

import java.util.List;

public interface FileTrigger {

    void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity);

    default FileCallback getFileCallback(String callbackType) {
        try {
            return SpringUtil.getBean(callbackType + "FileCallback");
        } catch (Exception e) {
            throw new OssFileException("根据callbackType获取FileCallback异常", e);
        }

        //return null;
    }

    default CallbackResponse callback(OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        return callback(ossDetail, ossTriggerEntity, null);
    }

    default CallbackResponse callback(OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity, String content) {
        String callbackTarget = ossTriggerEntity.getCallbackTarget();
        if (!callbackTarget.isEmpty()) {
            String[] split = callbackTarget.split(":");
            if (split.length > 0) {
                FileCallback fileCallback = getFileCallback(split[0]);
                String phone = null;
                String email = null;
                if (StrUtil.isNotBlank(content)) {
                    List<String> phones = ExtractUtil.getPhones(content, "CN");
                    List<String> emails = ExtractUtil.getEmail(content);
                    if (!phones.isEmpty() && !emails.isEmpty()) {
                        phone = phones.get(0);
                        email = emails.get(0);
                    }
                }
                OssUploadTriggerCallbackRequest request = toTriggerCallbackRequest(ossDetail, phone, email);
                return fileCallback.callback(request, ossTriggerEntity);
                /* else {
                    OssUploadCallbackRequest ossUploadCallbackRequest = buildCallbackRequest(ossDetail);
                    return fileCallback.callback(ossUploadCallbackRequest, ossTriggerEntity);
                }*/
            }
        }
        throw new OssFileException("未设置回调目标callbackTarget!格式为:[bean://beanName#method,http://xxxx/...]");
    }

    default OssUploadTriggerCallbackRequest toTriggerCallbackRequest(OssDetailEntity ossDetail, String phone, String email) {
        OssUploadTriggerCallbackRequest request = new OssUploadTriggerCallbackRequest();
        request.setMd5(ossDetail.getMd5());
        request.setOssFileId(ossDetail.getOssFileId());
        request.setOssDetailId(ossDetail.getOssDetailId());
        request.setAppId(ossDetail.getAppId());
        request.setTrigger(ossDetail.getTrigger());
        request.setFilePath(ossDetail.getFilePath());
        request.setFileExt(ossDetail.getFileExt());
        request.setFileLink(ossDetail.getFileLink());
        request.setFileSize(ossDetail.getFileSize());
        request.setStoreType(ossDetail.getChannel());
        request.setPhone(phone);
        request.setEmail(email);
        request.setOperatorId(ossDetail.getOperatorId());
        return request;
    }


    default OssUploadCallbackRequest buildCallbackRequest(OssDetailEntity ossDetail) {
        OssUploadCallbackRequest request = new OssUploadCallbackRequest();
        request.setMd5(ossDetail.getMd5());
        request.setOssFileId(ossDetail.getOssFileId());
        request.setAppId(ossDetail.getAppId());
        request.setFileName(ossDetail.getFileName());
        request.setOperatorId(ossDetail.getOperatorId());
        return request;
    }
}
