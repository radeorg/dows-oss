package org.dows.oss.handler;

import org.dows.oss.exception.OssFileException;
import org.dows.rade.pdf.PdfExtractor;
import org.dows.rade.pdf.ResumePdfData;
import org.springframework.stereotype.Component;

@Component
public class PythonPdfHandler implements Pdf2TxtHandler {
    @Override
    public String convert(String filePath) {
        // 使用Python执行PDF提取文本的命令
        try {
            ResumePdfData extract = PdfExtractor.extract(filePath, null);
            return extract.getContent();
        } catch (Exception e) {
            throw new OssFileException("调用python 解析pdf 异常！", e);
        }
    }
}
