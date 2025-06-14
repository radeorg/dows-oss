package org.dows.oss.feign;

import org.dows.oss.api.FileUploaderApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "oss-uploader") // 服务名和URL
public interface FileUploaderFeign extends FileUploaderApi {
}
