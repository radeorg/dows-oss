package org.dows.oss.feign;

import org.dows.oss.api.TencentOssApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "oss-tencent", configuration = OssFeignConfig.class) // 服务名和URL
public interface TencentOssApiFeign extends TencentOssApi {


}
