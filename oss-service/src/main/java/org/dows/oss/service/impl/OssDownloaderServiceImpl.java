package org.dows.oss.service.impl;


import org.dows.rade.crud.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.dows.oss.service.OssDownloaderService;
import org.dows.oss.entity.OssDownloaderEntity;
import org.dows.oss.mapper.OssDownloaderMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

/**
 * 服务层实现。
 *
 * @author lait.zhang@gmail.com
 * @since 1.0
 */
@Service
public class OssDownloaderServiceImpl extends BaseServiceImpl<OssDownloaderMapper, OssDownloaderEntity> implements OssDownloaderService {

}