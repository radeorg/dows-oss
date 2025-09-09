package org.dows.oss.trigger;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.dows.oss.callback.FileCallback;
import org.dows.oss.constant.PatternConstant;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.request.OssUploadCallbackRequest;
import org.dows.oss.request.OssUploadTriggerCallbackRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.oss.utils.CommonUtil;
import org.dows.rade.util.SpringUtil;

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
                    phone = CommonUtil.extractPattern(content, PatternConstant.PHONE_PATTERN);
                    phone = StringUtils.isNotEmpty(phone) ? formatPhoneNumber(phone) : "";
                    email = CommonUtil.extractPatterns(content, PatternConstant.EMAIL_REGEXES);
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

    private String formatPhoneNumber(String phone) {
        // 去除所有非数字字符
        String cleaned = phone.replaceAll("[^0-9]", "");

        // 去除中国大陆国际区号86前缀
        if (cleaned.startsWith("86")) {
            cleaned = cleaned.substring(2);
        }

        return cleaned;
    }
}
