package com.ysl.yslapicommon.service;

import com.ysl.yslapicommon.model.entity.InterfaceInfo;

/**
* @author ysl
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-09-08 23:28:03
*/
public interface InnerInterfaceInfoService{
    /**
     * 从数据库中查询模拟接口是否存在（请求路径，请求方法）
     * @param url
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String url, String method);
}
