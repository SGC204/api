package com.ysl.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysl.yslapicommon.model.entity.InterfaceInfo;

/**
* @author ysl
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-09-08 23:28:03
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     * @param interfaceInfo
     * @param add 是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
