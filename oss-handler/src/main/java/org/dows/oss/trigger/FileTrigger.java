package org.dows.oss.trigger;

import org.apache.commons.lang3.StringUtils;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.request.OssUploadTriggerCallbackRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.oss.util.ExtractUtil;
import org.dows.rade.util.SpringUtil;

public interface FileTrigger {

    void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity);

    default FileCallback getFileCallback(String callbackType) {
        if (StringUtils.isNotEmpty(callbackType)) {
            return SpringUtil.getBean(callbackType + "FileCallback");
        }
        return null;
    }

    default CallbackResponse callback(OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        return callback(ossDetail, ossTriggerEntity, null);
    }

    default CallbackResponse callback(OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity, String content) {
        String callbackTarget = ossTriggerEntity.getCallbackTarget();
        if (!callbackTarget.isEmpty()) {
            String[] split = callbackTarget.split(":");
            if (split.length > 0) {
                String phone = ExtractUtil.getPhones(content, "CN").getFirst();//CommonUtil.extractPattern(content, PatternConstant.PHONE_PATTERN);
                String email = ExtractUtil.getEmail(content).getFirst();
                OssUploadTriggerCallbackRequest request = toTriggerCallbackRequest(ossDetail, phone, email);

                FileCallback fileCallback = getFileCallback(split[0]);
                if (fileCallback != null) {
                    return fileCallback.callback(request, ossTriggerEntity);
                }
            }
        }
        return null;
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
}
