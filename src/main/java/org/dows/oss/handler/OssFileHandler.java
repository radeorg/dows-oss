package org.dows.oss.handler;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dows.oss.entity.OssUploaderEntity;
import org.dows.oss.entity.OssUploaderProcessEntity;
import org.dows.oss.pojo.enums.OssUploaderStateCodeEnum;
import org.dows.oss.reponse.CallbackBizResponse;
import org.dows.oss.reponse.QueryWaitDeleteResponse;
import org.dows.oss.reponse.QueryWaitProcessResponse;
import org.dows.oss.request.OssUploadRequest;
import org.dows.oss.request.QueryWaitCallbackRequest;
import org.dows.oss.request.QueryWaitDeleteRequest;
import org.dows.oss.request.QueryWaitProcessRequest;
import org.dows.rade.aac.AacUser;
import org.dows.rade.status.AuthStatusCode;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssFileHandler {

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
     * 获取待处理的文件
     */
    public OssUploaderEntity getWaitProcessFileByOssUploaderId(Long ossUploaderId){
        return QueryChain.of(OssUploaderEntity.class)
                .innerJoin(OssUploaderProcessEntity.class)
                .on(OssUploaderEntity::getOssUploaderId, OssUploaderProcessEntity::getOssUploaderId)
                .eq(OssUploaderEntity::getOssUploaderId, ossUploaderId)
                .eq(OssUploaderProcessEntity::getState, OssUploaderStateCodeEnum.WAIT_HANDLE.getCode())
                .one();
    }

    /**
     * 查询在数据库中存在的Md5
     */
    public List<String> queryExistUploaderFileMd5(OssUploadRequest ossUploadRequest){
        // 过滤出不重复、不为空的md5集合
        List<String> md5s = ossUploadRequest.getFileInfos().stream()
                .map(OssUploadRequest.OssUploadFileInfo::getMd5)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> fileMd5s = new ArrayList<>();
        List<OssUploaderEntity> ossUploaderEntities = QueryChain.of(OssUploaderEntity.class)
                .in(OssUploaderEntity::getFileMd5, md5s).list();
        if (ossUploaderEntities != null && !ossUploaderEntities.isEmpty()) {
            for (OssUploaderEntity ossUploaderEntity : ossUploaderEntities) {
                fileMd5s.add(ossUploaderEntity.getFileMd5());
            }
        }
        return fileMd5s;
    }

    /**
     * 获取登录人ID
     */
    public Long getAccountInstanceId (){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            throw new CredentialsExpiredException(AuthStatusCode.UNAUTHORIZED.getDescribe());
        }
        AacUser aacUser = (AacUser) principal;
        return aacUser.getAccountId();
    }
}
