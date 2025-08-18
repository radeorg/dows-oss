package org.dows.oss.handler;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * MinerU API处理类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MineruApiHandler {

    private final RestTemplate restTemplate;

    @Value("${mineru.api.url:https://mineru.net/api/v4/extract/task}")
    private String mineruApiUrl;

    @Value("${mineru.api.timeout:300000}") // 默认5分钟超时
    private long timeoutMs;

    @Value("${mineru.api.key}")
    private String mineruApiKey;

    /**
     * 提交PDF解析任务
     * @param fileUrl PDF文件URL
     * @return 任务ID
     */
    public String submitPdfParseTask(String fileUrl) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("url", fileUrl);
//            requestBody.put("output_format", "markdown");
            requestBody.put("is_ocr", true);
            requestBody.put("enable_formula", false);
            requestBody.put("enable_table", true);

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + mineruApiKey);
            headers.put("Content-Type", "application/json");

            // 创建请求实体
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    mineruApiUrl,
                    new HttpEntity<>(JSONUtil.parse(requestBody).toString(), new HttpHeaders() {{
                        setAll(headers);
                    }}),
                    String.class
            );
            log.info("---------------提交PDF解析任务返回参数：{}", response);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                if (jsonObject != null && jsonObject.containsKey("data")) {
                    return jsonObject.getJSONObject("data").getString("task_id");
                } else {
                    log.error("提交PDF解析任务失败: {}", responseBody);
                    throw new RuntimeException("提交PDF解析任务失败: " + responseBody);
                }
            } else {
                log.error("提交PDF解析任务失败, HTTP状态码: {}", response.getStatusCodeValue());
                throw new RuntimeException("提交PDF解析任务失败, HTTP状态码: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            log.error("提交PDF解析任务异常: {}", e.getMessage(), e);
            throw new RuntimeException("提交PDF解析任务异常: " + e.getMessage(), e);
        }
    }

    /**
     * 查询解析任务结果
     * @param taskId 任务ID
     * @return Markdown内容URL
     */
    public String queryParseResult(String taskId) {
        try {
            String url = mineruApiUrl + "/" + taskId;
            long startTime = System.currentTimeMillis();

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + mineruApiKey);
            headers.put("Content-Type", "application/json");

            // 创建请求实体
            org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
            httpHeaders.setAll(headers);
            HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);

            // 循环查询直到任务完成或超时
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                // 发送请求
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
                log.info("---------------查询解析任务结果返回参数：{}", response);

                if (response.getStatusCode().is2xxSuccessful()) {
                    String responseBody = response.getBody();
                    JSONObject jsonObject = JSONObject.parseObject(responseBody);
                    if (jsonObject != null && jsonObject.containsKey("data")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        String state = data.getString("state");
                        if ("done".equals(state)) {
                            return data.getString("full_zip_url");
                        } else if ("error".equals(state)) {
                            log.error("解析任务失败: {}", jsonObject.getString("message"));
                            throw new RuntimeException("解析任务失败: " + jsonObject.getString("message"));
                        } else {
                            // 任务未完成，等待后重试
                            Thread.sleep(2000); // 等待2秒后重试
                        }
                    } else {
                        // 任务未完成，等待后重试
                        Thread.sleep(2000); // 等待2秒后重试
                    }
                } else {
                    log.error("查询解析任务结果失败, HTTP状态码: {}", response.getStatusCodeValue());
                    throw new RuntimeException("查询解析任务结果失败, HTTP状态码: " + response.getStatusCodeValue());
                }
            }

            log.error("查询解析任务结果超时");
            throw new RuntimeException("查询解析任务结果超时");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("查询解析任务结果被中断: {}", e.getMessage());
            throw new RuntimeException("查询解析任务结果被中断: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("查询解析任务结果异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询解析任务结果异常: " + e.getMessage(), e);
        }
    }

    /**
     * 下载Markdown内容
     * @param zipUrl 包含Markdown的ZIP文件URL
     * @return Markdown内容
     */
    public String downloadMarkdown(String zipUrl) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + mineruApiKey);

            // 创建请求实体
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAll(headers);
            HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);

            // 发送请求下载ZIP文件
            ResponseEntity<byte[]> response = restTemplate.exchange(zipUrl, HttpMethod.GET, requestEntity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                byte[] zipBytes = response.getBody();
                // 创建临时文件存储ZIP内容
                java.io.File tempZip = java.io.File.createTempFile("mineru_", ".zip");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempZip)) {
                    fos.write(zipBytes);
                }

                // 解压ZIP文件
                String markdownContent = null;
                try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(tempZip)) {
                    java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        java.util.zip.ZipEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".md")) {
                            try (java.io.BufferedReader br = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(zipFile.getInputStream(entry), "UTF-8"))) {
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    sb.append(line).append("\n");
                                }
                                markdownContent = sb.toString();
                                break;
                            }
                        }
                    }
                }

                // 删除临时文件
                tempZip.delete();

                if (markdownContent != null) {
                    return markdownContent;
                } else {
                    log.error("ZIP文件中未找到Markdown文件");
                    throw new RuntimeException("ZIP文件中未找到Markdown文件");
                }
            } else {
                log.error("下载ZIP文件失败, HTTP状态码: {}", response.getStatusCodeValue());
                throw new RuntimeException("下载ZIP文件失败, HTTP状态码: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            log.error("下载Markdown内容异常: {}", e.getMessage(), e);
            throw new RuntimeException("下载Markdown内容异常: " + e.getMessage(), e);
        }
    }
}