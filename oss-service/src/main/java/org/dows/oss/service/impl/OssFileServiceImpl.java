package org.dows.oss.service.impl;


import org.dows.rade.crud.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.dows.oss.service.OssFileService;
import org.dows.oss.entity.OssFileEntity;
import org.dows.oss.mapper.OssFileMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

/**
 * 服务层实现。
 *
 * @author lait.zhang@gmail.com
 * @since 1.0
 */
@Service
public class OssFileServiceImpl extends BaseServiceImpl<OssFileMapper, OssFileEntity> implements OssFileService {

}