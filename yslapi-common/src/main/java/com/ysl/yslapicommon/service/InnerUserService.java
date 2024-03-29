package com.ysl.yslapicommon.service;

import com.ysl.yslapicommon.model.entity.User;

/**
 * 用户服务
 *
 * @author ysl
 */
public interface InnerUserService{

    /**
     * 数据库中查是否分配给用户秘钥（ak，sk）
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);
}
