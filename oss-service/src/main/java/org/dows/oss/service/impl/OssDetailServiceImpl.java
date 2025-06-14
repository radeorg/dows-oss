package org.dows.oss.service.impl;


import org.dows.rade.crud.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.dows.oss.service.OssDetailService;
import org.dows.oss.entity.OssDetailEntity;
import org.dows.oss.mapper.OssDetailMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

/**
 * 服务层实现。
 *
 * @author lait.zhang@gmail.com
 * @since 1.0
 */
@Service
public class OssDetailServiceImpl extends BaseServiceImpl<OssDetailMapper, OssDetailEntity> implements OssDetailService {

}