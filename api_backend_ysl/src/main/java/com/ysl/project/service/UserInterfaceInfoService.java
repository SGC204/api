package com.ysl.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysl.yslapicommon.model.entity.UserInterfaceInfo;

/**
* @author ysl
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2023-09-22 14:25:55
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
