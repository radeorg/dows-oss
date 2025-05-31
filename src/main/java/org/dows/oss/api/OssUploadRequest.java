package org.dows.oss.api;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
public class OssUploadRequest {

    private Map<Long, MultipartFile> files;
//    channel:cos,
//    appid:1,
//    source：hrm，
//    path:xxx,
//    trigger:ott,
//    callback:http:xxx?txt=xxx&filelink=xxx
//    secretKey:xxx,
//    secretId:xxxx
}
