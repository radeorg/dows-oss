package org.dows.oss.service.impl;


import org.dows.rade.crud.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.dows.oss.service.OssIdentifierService;
import org.dows.oss.entity.OssIdentifierEntity;
import org.dows.oss.mapper.OssIdentifierMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

/**
 * 服务层实现。
 *
 * @author lait.zhang@gmail.com
 * @since 1.0
 */
@Service
public class OssIdentifierServiceImpl extends BaseServiceImpl<OssIdentifierMapper, OssIdentifierEntity> implements OssIdentifierService {

}