package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.handler.MineruApiHandler;
import org.dows.oss.handler.OssDetailHandler;
import org.dows.oss.handler.OssUploaderHandler;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.rade.oss.OssInfo;
import org.springframework.stereotype.Component;


/**
 * 文件上传云服务触发器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FileUploadedTrigger implements FileTrigger {

    private final OssUploaderHandler ossUploaderHandler;
    private final OssDetailHandler ossDetailHandler;
    private final MineruApiHandler mineruApiHandler;

    @Override
    public void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        log.info("文件上传云服务触发器：{}", ossTriggerEntity.getTrigger());

        try {
            // 文件上传云服务
            OssUploadHandlerRequest request = ossDetailHandler.toOssUploadHandlerRequest(ossFile, ossDetail);
            OssInfo info = ossUploaderHandler.uploadOriginalFile(request);
            if (info != null) {
                // 更新文件上传链接信息
                ossDetailHandler.updateOssDetailFileInfo(info, ossDetail);

                // 如果是PDF文件，则调用Mineru API解析为Markdown
                if ("pdf".equalsIgnoreCase(ossFile.getFileExt())) {
                    try {
                        // 提交PDF解析任务
                        String taskId = mineruApiHandler.submitPdfParseTask(info.getFileLink());
                        log.info("PDF解析任务提交成功，taskId: {}", taskId);

                        // 查询解析任务结果
                        String markdownUrl = mineruApiHandler.queryParseResult(taskId);
                        log.info("PDF解析任务完成，markdownUrl: {}", markdownUrl);

                        // 下载Markdown内容
                        String markdownContent = mineruApiHandler.downloadMarkdown(markdownUrl);
                        log.info("Markdown内容下载成功，长度: {}", markdownContent.length());

                        // 上传Markdown内容到COS
                        OssUploadHandlerRequest mdRequest = new OssUploadHandlerRequest();
                        mdRequest.setMd5(ossFile.getMd5());
                        mdRequest.setFileExt("md");
                        mdRequest.setFilePath(ossDetail.getBasePath());
                        mdRequest.setChannel(ossDetail.getChannel());
                        OssInfo mdInfo = ossUploaderHandler.uploadFileContent(mdRequest, markdownContent);
                        if (mdInfo != null) {
                            log.info("Markdown内容上传成功，fileUrl: {}", mdInfo.getFileLink());
                            // 可以在这里更新数据库，存储Markdown文件信息
                        }
                    } catch (Exception e) {
                        log.error("PDF解析为Markdown失败: {}", e.getMessage(), e);
                        // 解析失败不影响主流程，记录日志即可
                    }
                }

                // 回调业务系统
                CallbackResponse callbackResponse = callback(ossDetail, ossTriggerEntity);

                // 更新文件回调信息
                ossDetailHandler.updateOssDetailCallbackInfo(ossDetail, callbackResponse);
            }
        } catch (Exception e) {
            log.error("文件上传云服务触发器异常:{}", e.getMessage());
            throw new OssFileException(e.getMessage());
        }
    }
}
