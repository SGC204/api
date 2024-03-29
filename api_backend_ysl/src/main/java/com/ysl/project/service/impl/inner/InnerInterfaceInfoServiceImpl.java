package com.ysl.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysl.project.common.ErrorCode;
import com.ysl.project.exception.BusinessException;
import com.ysl.project.mapper.InterfaceInfoMapper;
import com.ysl.yslapicommon.model.entity.InterfaceInfo;
import com.ysl.yslapicommon.service.InnerInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        if (StringUtils.isAnyBlank(url, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        interfaceInfoQueryWrapper.eq("url", url);
        interfaceInfoQueryWrapper.eq("method", method);
        return interfaceInfoMapper.selectOne(interfaceInfoQueryWrapper);
    }
}
