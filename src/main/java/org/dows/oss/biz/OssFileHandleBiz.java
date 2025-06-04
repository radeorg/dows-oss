package org.dows.oss.biz;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.constant.OssUploaderStateCodeConstant;
import org.dows.oss.entity.OssUploaderEntity;
import org.dows.oss.reponse.CallbackBizResponse;
import org.dows.oss.reponse.QuerySchedulerOssUploadResponse;
import org.dows.oss.api.OssUploadRequest;
import org.dows.oss.request.QuerySchedulerOssUploadRequest;
import org.dows.oss.service.OssUploaderService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            e.printStackTrace();
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
        int sucessNum = 0;
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
                        + getDate("yyMMdd");
                File dest = new File(filePath + File.separator + md5 + fileName.substring(fileName.lastIndexOf(".")));
                File parentDir = dest.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                try {
                    file.transferTo(dest);
                    saveOssUploader(ossUploadRequest, filePath, fileName, dest, idx);
                    sucessNum++;
                } catch (Exception e) {
                    log.error("文件上传失败: {}", fileName, e);
                    if (dest.exists()) {
                        dest.delete();
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
        result.put("successNum", sucessNum);
        result.put("failNum", files.length - sucessNum);
        result.put("failInfo", failInfo);
        return result;
    }

    /**
     * 将本地文件上传至COS服务
     */
    public void uploadLocalFileToCos(List<QuerySchedulerOssUploadResponse> responses) throws FileNotFoundException {
        if (responses != null && !responses.isEmpty()) {
            for (QuerySchedulerOssUploadResponse response : responses) {
                String fileName = File.separator + response.getFileMd5() + response.getFileExt();
                FileInputStream file = new FileInputStream(response.getFileTempPath() + fileName);
                String savePath = response.getFileBasePath() + File.separator + getDate("yyMMdd") + File.separator + fileName;
                OssInfo info = tencentOssClient.upLoad(new BufferedInputStream(file), savePath, false);

                StringBuilder sb = new StringBuilder(response.getStateCode());
                sb.setCharAt(0, '1');  // 直接修改指定位置字符

                OssUploaderEntity entity = new OssUploaderEntity();
                entity.setOssUploaderId(response.getOssUploaderId());
                entity.setStateCode(sb.toString());
                entity.setFileLink(info.getFilePath());
                ossUploaderService.updateById(entity);
            }
        }
    }

    /**
     * 将本地文件进行解析，将解析出来的格式化的文本存储到COS服务
     */
    public void parseLocalFileToCos(List<QuerySchedulerOssUploadResponse> responses){
        // TODO 解析上传COS
    }

    /**
     * 查询待回调文件
     */
    public Page<CallbackBizResponse> queryOssUploadFile(QuerySchedulerOssUploadRequest request){
        Page<CallbackBizResponse> page = new Page<>(
                Long.valueOf(request.getPageNum()),
                Long.valueOf(request.getPageSize())
        );
        return
                QueryChain.of(OssUploaderEntity.class)
                        .select(
                                OssUploaderEntity::getOssUploaderId,
                                OssUploaderEntity::getBizId,
                                OssUploaderEntity::getAppId,
                                OssUploaderEntity::getSource,
                                OssUploaderEntity::getTrigger,
                                OssUploaderEntity::getFileLink,
                                OssUploaderEntity::getFileMd5,
                                OssUploaderEntity::getTxtLink,
                                OssUploaderEntity::getFileSize,
                                OssUploaderEntity:: getCallbackUrl
                        )
                        .eq(OssUploaderEntity::getCallbackState, request.getCallbackState())
                        .and(q -> {
                            q.where(OssUploaderEntity::getStateCode).likeLeft("11")
                                    .and(OssUploaderEntity::getTrigger).like("OTT");
                            q.or(OssUploaderEntity::getStateCode).likeLeft("10")
                                    .and(OssUploaderEntity::getTrigger).notLike("OTT");
                        })
                        .orderBy(OssUploaderEntity::getUt)
                        .asc()
                        .pageAs(page, CallbackBizResponse.class);

    }

    /**
     * 将已执行完毕（上传+解析）的文件进行回调，返回业务系统文件地址等信息
     */
    public void callbackBiz(List<CallbackBizResponse> responses){
        if (responses != null && !responses.isEmpty()) {
            for (CallbackBizResponse response : responses) {
                ResponseEntity<String> callbackResponse = new RestTemplate().postForEntity(response.getCallbackUrl(), response, String.class);
                OssUploaderEntity entity = new OssUploaderEntity();
                entity.setOssUploaderId(response.getOssUploaderId());
                System.out.println(callbackResponse.getStatusCode());
                if (callbackResponse.getStatusCode().toString().equals("200 OK")) {
                    entity.setCallbackState(1);
                } else {
                    entity.setCallbackState(2);
                }
                ossUploaderService.updateById(entity);
            }
        }
    }

    /**
     * 查询本地文件
     */
    public Page<QuerySchedulerOssUploadResponse> queryLocalFile(QuerySchedulerOssUploadRequest request){
        Page<QuerySchedulerOssUploadResponse> page = new Page<>(
                Long.valueOf(request.getPageNum()),
                Long.valueOf(request.getPageSize())
        );
        return QueryChain.of(OssUploaderEntity.class)
                .select(OssUploaderEntity::getOssUploaderId,
                        OssUploaderEntity:: getFileBasePath,
                        OssUploaderEntity:: getFileTempPath,
                        OssUploaderEntity:: getFileMd5,
                        OssUploaderEntity:: getFileExt,
                        OssUploaderEntity:: getTxtBasePath,
                        OssUploaderEntity::getStateCode,
                        OssUploaderEntity:: getExpireDate)
                .eq(OssUploaderEntity::getStateCode, request.getStateCode(), request.getStateCodeType() != null && request.getStateCodeType().equals(OssUploaderStateCodeConstant.STATE_TYPE_EQ))
                .likeLeft(OssUploaderEntity::getStateCode, request.getStateCode(), request.getStateCodeType() != null && request.getStateCodeType().equals(OssUploaderStateCodeConstant.STATE_TYPE_LEFT_LIKE))
                .ge(OssUploaderEntity::getExpireDate, request.getStartTime(), request.getStartTime() != null)
                .le(OssUploaderEntity::getExpireDate, request.getEndTime(), request.getEndTime() != null)
                .orderBy(OssUploaderEntity::getUt)
                .asc()
                .pageAs(page, QuerySchedulerOssUploadResponse.class);
    }

    /**
     * 删除本地文件
     */
    public void deleteLocalFile(List<QuerySchedulerOssUploadResponse> responses){
        if (responses != null && !responses.isEmpty()) {
            for (QuerySchedulerOssUploadResponse response : responses) {
                File file = new File(response.getFileTempPath()
                        + File.separator
                        + response.getFileMd5()
                        + response.getFileExt());
                if (file.exists()) {
                    file.delete();
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

    private String getDate(String format){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.now().format(formatter);
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
        ossUploaderEntity.setFileBasePath(ossUploadRequest.getFilePath());
        ossUploaderEntity.setTxtBasePath(ossUploadRequest.getTxtPath());
        ossUploaderEntity.setTrigger(ossUploadRequest.getTrigger().toString());
        ossUploaderEntity.setCallbackUrl(ossUploadRequest.getCallbackUrl());
        ossUploaderEntity.setSource(ossUploadRequest.getSource());
        ossUploaderEntity.setExpireTime(ossUploadRequest.getExpireTime());
        ossUploaderEntity.setExpireDate(new Date(System.currentTimeMillis() + ossUploadRequest.getExpireTime()));
        ossUploaderService.save(ossUploaderEntity);
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
}
