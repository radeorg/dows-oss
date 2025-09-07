package org.dows.oss.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dows.oss.constant.PatternConstant;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.entity.OssTriggerEntity;
import org.dows.oss.exception.OssFileException;
import org.dows.oss.handler.MineruPdfHandler;
import org.dows.oss.handler.OssDetailHandler;
import org.dows.oss.handler.OssUploaderHandler;
import org.dows.oss.handler.Pdf2TxtHandler;
import org.dows.oss.request.OssUploadHandlerRequest;
import org.dows.oss.response.CallbackResponse;
import org.dows.oss.util.PdfParseMarkDownUtil;
import org.dows.oss.utils.CommonUtil;
import org.dows.oss.utils.DocxToMdConverterUtil;
import org.dows.rade.oss.OssInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件转换触发器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MdTransformTrigger implements FileTrigger {
    private static final List<String> SUPPORTED_FILE_EXTENSIONS = List.of(".doc", ".docx", ".pdf");

    private final OssUploaderHandler ossUploaderHandler;
    private final OssDetailHandler ossDetailHandler;
    private final MineruPdfHandler mineruPdfHandler;

    private final Pdf2TxtHandler pythonPdfHandler;

    @Override
    public void trigger(OssFileEntity ossFile, OssDetailEntity ossDetail, OssTriggerEntity ossTriggerEntity) {
        log.info("MD文件转换触发器：{}", ossTriggerEntity.getTrigger());

        try {
            // 解析文本
            OssUploadHandlerRequest request = ossDetailHandler.toOssUploadHandlerRequest(ossFile, ossDetail);
            request.setFileExt(".md");

            String filePath = ossUploaderHandler.presignedCosViewUrl(ossDetail.getFilePath());
            String parseContent = pdfConvertToMarkdown(ossFile.getFileExt(), filePath, request.getFileLocalPath());

            // 文本上传云服务
            OssInfo info = ossUploaderHandler.uploadFileContent(request, parseContent);
            if (info != null) {
                System.out.println("------------" + info.getFilePath());
                // 更新文件上传链接信息
                ossDetailHandler.updateOssDetailFileInfo(info, ossDetail);

                // 回调业务系统
                CallbackResponse callbackResponse = callback(ossDetail, ossTriggerEntity, parseContent);

                // 更新文件回调信息
                ossDetailHandler.updateOssDetailCallbackInfo(ossDetail, callbackResponse);
            }
        } catch (Exception e) {
            log.error("文件上传云服务触发器异常:{}", e.getMessage());
            throw new OssFileException(e.getMessage());
        }
    }

    private String pdfConvertToMarkdown(String fileExt, String filePath, String localFilePath) {
        // 如果是PDF文件，则调用Mineru API解析为Markdown
        if (SUPPORTED_FILE_EXTENSIONS.contains(fileExt)) {
            String content = "";
            try {
                if (".pdf".equals(fileExt)) {
                    content = PdfParseMarkDownUtil.convertToMarkdown(localFilePath);
                } else {
                    content = DocxToMdConverterUtil.convertToMarkdown(localFilePath);
                }
            } catch (Exception e) {
                log.error("PDF解析为Markdown失败: {}", e.getMessage(), e);
//                return pythonAnalysePdf(filePath);
                throw new OssFileException("PDF解析失败！");
            }

            String phone = CommonUtil.extractPattern(content, PatternConstant.PHONE_PATTERN);
            String email = CommonUtil.extractPattern(content, PatternConstant.EMAIL_PATTERN);
//            if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(email)) {
//                return pythonAnalysePdf(filePath);
//            }
            if (StringUtils.isEmpty(phone)) {
                throw new OssFileException("未识别到手机号，无效简历！");
            } else if (StringUtils.isEmpty(email)) {
                throw new OssFileException("未识别到邮箱，无效简历！");
            }

            return content;
        } else {
            throw new OssFileException("暂不支持该类型文件转换");
        }
    }

    private String pythonAnalysePdf(String filePath){
        try {
            return pythonPdfHandler.convert(filePath);
        } catch (Exception ex) {
            try {
                // 提交PDF解析任务
                String taskId = mineruPdfHandler.submitPdfParseTask(filePath);
                log.info("PDF解析任务提交成功，taskId: {}", taskId);

                // 查询解析任务结果
                String markdownUrl = mineruPdfHandler.queryParseResult(taskId);
                log.info("PDF解析任务完成，markdownUrl: {}", markdownUrl);

                // 下载Markdown内容
                String markdownContent = mineruPdfHandler.downloadMarkdown(markdownUrl);
                log.info("Markdown内容下载成功，长度: {}", markdownContent.length());

                return markdownContent;
            } catch (Exception exc) {
                log.error("PDF解析为Markdown失败: {}", ex.getMessage(), ex);
                throw new OssFileException(exc.getMessage());
            }
        }
    }
}
