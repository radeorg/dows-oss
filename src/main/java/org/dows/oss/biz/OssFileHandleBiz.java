package org.dows.oss.biz;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssUploaderEntity;
import org.dows.oss.entity.OssUploaderProcessEntity;
import org.dows.oss.pojo.enums.OssUploaderCallBackStateEnum;
import org.dows.oss.pojo.enums.OssUploaderStateCodeEnum;
import org.dows.oss.reponse.CallbackBizResponse;
import org.dows.oss.reponse.QueryWaitProcessResponse;
import org.dows.oss.reponse.QueryWaitDeleteResponse;
import org.dows.oss.request.OssUploadRequest;
import org.dows.oss.request.QueryWaitDeleteRequest;
import org.dows.oss.request.QueryWaitCallbackRequest;
import org.dows.oss.request.QueryWaitProcessRequest;
import org.dows.oss.service.OssUploaderProcessService;
import org.dows.oss.service.OssUploaderService;
import org.dows.oss.utils.CommonUtil;
import org.dows.oss.utils.FileParseUtil;
import org.dows.rade.aac.AacUser;
import org.dows.rade.oss.OssInfo;
import org.dows.rade.oss.tencent.TencentOssClient;
import org.dows.rade.status.AuthStatusCode;
import org.dows.rade.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssFileHandleBiz {

    @Value("${rade.oss.modulePath.uim:/uim}")
    private String orgImgPath;
    @Value("${rade.oss.path:/radeorg}")
    private String orgPath;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private final TencentOssClient tencentOssClient;
    private final OssUploaderService ossUploaderService;
    private final OssUploaderProcessService ossUploaderProcessService;

    /**
     * 上传图片到COS
     * @param file 上传的文件
     */
    public OssInfo uploadImgToCos(MultipartFile file){
        String fileName = getFileName(file);
        checkFileExtension(fileName);
        String savePath = String.format("%s/%s.%s", orgImgPath, UUID.randomUUID(), fileName.substring(fileName.lastIndexOf(".")+1));
        try {
            return tencentOssClient.upLoad(file.getInputStream(), savePath, false);
        } catch (IOException e) {
            log.error("上传文件失败: {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据图片路径获取临时图片预览地址
     * @param filePath 图片路径
     * @return 临时图片预览地址
     */
    public String presignedViewUrl(String filePath){
        return tencentOssClient.presignedViewUrl(filePath, 5 * 60L);
    }

    /**
     * 上传文件到本地
     * @param files 上传的文件
     * @param request 请求参数
     */
    public Map<String, Object> uploadFileToLocal(MultipartFile[] files, String request){
        int idx = 0;
        int successNum = 0;
        Map<Long, String> failInfo = new HashMap<>();
        List<String> distinctMd5s = new ArrayList<>();
        OssUploadRequest ossUploadRequest = JsonUtil.toObject(request, OssUploadRequest.class);
        List<String> ossUploadFileMd5s = getOssUploaderFileMd5(ossUploadRequest);
        for (MultipartFile file : files) {
            String md5 = ossUploadRequest.getMd5s().get(idx);
            if (!distinctMd5s.contains(md5) && !ossUploadFileMd5s.contains(md5)) {
                String fileName = getFileName(file);
                String filePath = System.getProperty("user.home")
                        + orgPath + File.separator
                        + ossUploadRequest.getSource() + File.separator
                        + CommonUtil.formatDate(new Date(), "yyMMdd");
                File dest = new File(filePath + File.separator + md5 + fileName.substring(fileName.lastIndexOf(".")));
                File parentDir = dest.getParentFile();
                try {
                    if (!parentDir.exists()) {
                        if (!parentDir.mkdirs()) {
                            throw new IOException("目录创建失败: " + parentDir.getAbsolutePath());
                        }
                    }
                    file.transferTo(dest);
                    saveOssUploader(ossUploadRequest, filePath, fileName, dest, idx);
                    successNum++;
                } catch (Exception e) {
                    log.error("文件上传失败: {}", fileName, e);
                    if (dest.exists() && !dest.delete()) {
                        log.error("文件删除失败: {}", dest.getAbsolutePath());
                    }
                    failInfo.put(ossUploadRequest.getBizIds().get(idx), e.getMessage());
                }
            } else {
                failInfo.put(ossUploadRequest.getBizIds().get(idx), "文件重复");
            }
            if (!distinctMd5s.contains(md5)) {
                distinctMd5s.add(md5);
            }
            idx++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalNum", files.length);
        result.put("successNum", successNum);
        result.put("failNum", files.length - successNum);
        result.put("failInfo", failInfo);
        return result;
    }

    /**
     * 将本地文件上传至COS服务
     */
    public void uploadLocalFileToCos(List<QueryWaitProcessResponse> responses) throws FileNotFoundException {
        if (responses != null && !responses.isEmpty()) {
            for (QueryWaitProcessResponse response : responses) {
                String fileName = File.separator + response.getFileMd5() + response.getFileExt();
                try {
                    FileInputStream file = new FileInputStream(response.getFileTempPath() + fileName);
                    String savePath = response.getFilePath() + File.separator
                            + CommonUtil.formatDate(new Date(), "yyMMdd") + File.separator
                            + fileName;
                    OssInfo info = tencentOssClient.upLoad(new BufferedInputStream(file), savePath, false);

                    updateOssUploaderProcessSuccess(response.getOssUploaderProcessId());

                    OssUploaderEntity entity = new OssUploaderEntity();
                    entity.setOssUploaderId(response.getOssUploaderId());
                    entity.setFileLink(info.getFileLink());
                    entity.setFileBasePath(info.getFilePath());
                    if (getWaitProcessFileByOssUploaderId(response.getOssUploaderId()) == null) {
                        // 没有待处理的状态，则更新为已完成
                        entity.setState(OssUploaderStateCodeEnum.COMPLETE_HANDLE.getCode());
                    }
                    ossUploaderService.updateById(entity);
                } catch (Exception e){
                    log.error("文件上传失败: {}", fileName, e);

                    updateOssUploaderProcessFailed(response.getOssUploaderProcessId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 将本地文件进行解析，将解析出来的格式化的文本存储到COS服务
     */
    public void parseLocalFileToCos(List<QueryWaitProcessResponse> responses) {
        if (responses != null && !responses.isEmpty()) {
            for (QueryWaitProcessResponse response : responses) {
                String fileName = response.getFileTempPath() + File.separator + response.getFileMd5() + response.getFileExt();
                try {
                    String savePath = response.getTxtPath() + File.separator
                            + CommonUtil.formatDate(new Date(), "yyMMdd") + File.separator
                            + response.getFileMd5() + ".txt";
                    String parseContent = FileParseUtil.convertToMarkdown(fileName);
                    OssInfo info = tencentOssClient.upLoad(new ByteArrayInputStream(parseContent.getBytes()), savePath, false);

                    updateOssUploaderProcessSuccess(response.getOssUploaderProcessId());

                    OssUploaderEntity entity = new OssUploaderEntity();
                    entity.setOssUploaderId(response.getOssUploaderId());
                    entity.setTxtLink(info.getFileLink());
                    entity.setTxtBasePath(info.getFilePath());
                    if (getWaitProcessFileByOssUploaderId(response.getOssUploaderId()) == null) {
                        // 没有待处理的状态，则更新为已完成
                        entity.setState(OssUploaderStateCodeEnum.COMPLETE_HANDLE.getCode());
                    }
                    ossUploaderService.updateById(entity);
                } catch (Exception e){
                    log.error("文件解析失败: {}", fileName, e);

                    updateOssUploaderProcessFailed(response.getOssUploaderProcessId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 将已执行完毕（上传+解析）的文件进行回调，返回业务系统文件地址等信息
     */
    public void callbackBiz(List<CallbackBizResponse> responses){
        if (responses != null && !responses.isEmpty()) {
            for (CallbackBizResponse response : responses) {
                OssUploaderEntity entity = new OssUploaderEntity();
                entity.setOssUploaderId(response.getOssUploaderId());
                try {
                    ResponseEntity<String> callbackResponse = new RestTemplate().postForEntity(response.getCallbackUrl(), response, String.class);
                    if (callbackResponse.getStatusCode().toString().equals("200 OK")) {
                        entity.setCallbackState(OssUploaderCallBackStateEnum.COMPLETE_CALLBACK.getCode());
                    } else {
                        entity.setCallbackState(OssUploaderCallBackStateEnum.FAILED_CALLBACK.getCode());
                    }
                }  catch (Exception e) {
                    log.error("回调失败", e);
                    entity.setCallbackState(OssUploaderCallBackStateEnum.FAILED_CALLBACK.getCode());
                }
                ossUploaderService.updateById(entity);
            }
        }
    }

    /**
     * 查询待处理的文件
     */
    public Page<QueryWaitProcessResponse> queryWaitProcessFiles(QueryWaitProcessRequest request){
        Page<QueryWaitProcessResponse> page = new Page<>(
                Long.valueOf(request.getPageNum()),
                Long.valueOf(request.getPageSize())
        );
        return QueryChain.of(OssUploaderEntity.class)
                .innerJoin(OssUploaderProcessEntity.class)
                .on(OssUploaderEntity::getOssUploaderId, OssUploaderProcessEntity::getOssUploaderId)
                .eq(OssUploaderProcessEntity::getState, request.getState())
                .eq(OssUploaderProcessEntity::getTrigger, request.getTrigger())
                .orderBy(OssUploaderEntity::getUt)
                .asc()
                .pageAs(page, QueryWaitProcessResponse.class);
    }

    /**
     * 查询待回调文件
     */
    public Page<CallbackBizResponse> queryWaitCallbackFiles(QueryWaitCallbackRequest request){
        Page<CallbackBizResponse> page = new Page<>(
                Long.valueOf(request.getPageNum()),
                Long.valueOf(request.getPageSize())
        );
        return QueryChain.of(OssUploaderEntity.class)
                        .eq(OssUploaderEntity::getCallbackState, request.getCallbackState())
                        .eq(OssUploaderEntity::getState, request.getState())
                        .orderBy(OssUploaderEntity::getUt)
                        .asc()
                        .pageAs(page, CallbackBizResponse.class);

    }

    /**
     * 查询待删除的文件
     */
    public Page<QueryWaitDeleteResponse> queryWaitDeleteFiles(QueryWaitDeleteRequest request){
        Page<QueryWaitDeleteResponse> page = new Page<>(
                Long.valueOf(request.getPageNum()),
                Long.valueOf(request.getPageSize())
        );
        return QueryChain.of(OssUploaderEntity.class)
                .ge(OssUploaderEntity::getExpireDate, request.getStartTime(), request.getStartTime() != null)
                .le(OssUploaderEntity::getExpireDate, request.getEndTime(), request.getEndTime() != null)
                .orderBy(OssUploaderEntity::getUt)
                .asc()
                .pageAs(page, QueryWaitDeleteResponse.class);
    }

    /**
     * 删除本地文件
     */
    public void deleteLocalFile(List<QueryWaitDeleteResponse> responses){
        if (responses != null && !responses.isEmpty()) {
            for (QueryWaitDeleteResponse response : responses) {
                try{
                    File file = new File(response.getFileTempPath()
                            + File.separator
                            + response.getFileMd5()
                            + response.getFileExt());
                    if (file.exists() && !file.delete()) {
                        log.error("文件删除失败: {}", file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.error("文件删除失败: {}", response.getFileTempPath(), e);
                }
            }
        }
    }

    private String getFileName(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        return fileName;
    }

    private void checkFileExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
    }

    private List<String> getOssUploaderFileMd5(OssUploadRequest ossUploadRequest){
        List<String> fileMd5s = new ArrayList<>();
        List<OssUploaderEntity> ossUploaderEntities = QueryChain.of(OssUploaderEntity.class)
                .in(OssUploaderEntity::getFileMd5, ossUploadRequest.getMd5s()).list();
        if (ossUploaderEntities != null && !ossUploaderEntities.isEmpty()) {
            for (OssUploaderEntity ossUploaderEntity : ossUploaderEntities) {
                fileMd5s.add(ossUploaderEntity.getFileMd5());
            }
        }
        return fileMd5s;
    }

    private void saveOssUploader(OssUploadRequest ossUploadRequest, String filePath, String fileName, File dest, int idx){
        OssUploaderEntity ossUploaderEntity = new OssUploaderEntity();
        ossUploaderEntity.setBizId(ossUploadRequest.getBizIds().get(idx));
        ossUploaderEntity.setAccountInstanceId(getAccountInstanceId());
        ossUploaderEntity.setFileTempPath(filePath);
        ossUploaderEntity.setFileName(fileName);
        ossUploaderEntity.setFileMd5(ossUploadRequest.getMd5s().get(idx));
        ossUploaderEntity.setFileExt(fileName.substring(fileName.lastIndexOf(".")));
        ossUploaderEntity.setFileSize(dest.length());
        ossUploaderEntity.setFilePath(ossUploadRequest.getFilePath());
        ossUploaderEntity.setTxtPath(ossUploadRequest.getTxtPath());
        ossUploaderEntity.setTrigger(ossUploadRequest.getTrigger().toString());
        ossUploaderEntity.setCallbackUrl(ossUploadRequest.getCallbackUrl());
        ossUploaderEntity.setSource(ossUploadRequest.getSource());
        ossUploaderEntity.setExpireTime(ossUploadRequest.getExpireTime());
        ossUploaderEntity.setExpireDate(new Date(System.currentTimeMillis() + ossUploadRequest.getExpireTime()));
        ossUploaderService.save(ossUploaderEntity);

        for (String trigger : ossUploadRequest.getTrigger()) {
            saveOssUploaderProcess(ossUploaderEntity, trigger);
        }
    }

    private void saveOssUploaderProcess(OssUploaderEntity ossUploaderEntity, String trigger){
        OssUploaderProcessEntity entity = new OssUploaderProcessEntity();
        entity.setOssUploaderId(ossUploaderEntity.getOssUploaderId());
        entity.setAccountInstanceId(ossUploaderEntity.getAccountInstanceId());
        entity.setTrigger(trigger);
        ossUploaderProcessService.save(entity);
    }

    private Long getAccountInstanceId (){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            throw new CredentialsExpiredException(AuthStatusCode.UNAUTHORIZED.getDescribe());
        }
        AacUser aacUser = (AacUser) principal;
        return aacUser.getAccountId();
    }

    private OssUploaderEntity getWaitProcessFileByOssUploaderId(Long ossUploaderId){
        return QueryChain.of(OssUploaderEntity.class)
                .innerJoin(OssUploaderProcessEntity.class)
                .on(OssUploaderEntity::getOssUploaderId, OssUploaderProcessEntity::getOssUploaderId)
                .eq(OssUploaderEntity::getOssUploaderId, ossUploaderId)
                .eq(OssUploaderProcessEntity::getState, OssUploaderStateCodeEnum.WAIT_HANDLE.getCode())
                .one();
    }

    private void updateOssUploaderProcessSuccess(Long ossUploaderProcessId){
        OssUploaderProcessEntity processEntity = new OssUploaderProcessEntity();
        processEntity.setOssUploaderProcessId(ossUploaderProcessId);
        processEntity.setState(OssUploaderStateCodeEnum.COMPLETE_HANDLE.getCode());
        ossUploaderProcessService.updateById(processEntity);
    }

    private void updateOssUploaderProcessFailed(Long ossUploaderProcessId, String failedReason){
        OssUploaderProcessEntity processEntity = new OssUploaderProcessEntity();
        processEntity.setOssUploaderProcessId(ossUploaderProcessId);
        processEntity.setState(OssUploaderStateCodeEnum.FAILED_HANDLE.getCode());
        processEntity.setFailedReason(failedReason);
        ossUploaderProcessService.updateById(processEntity);
    }
}
