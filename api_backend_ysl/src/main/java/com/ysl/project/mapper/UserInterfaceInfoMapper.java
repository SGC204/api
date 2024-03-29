package com.ysl.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ysl.yslapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author ysl
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-09-22 14:25:55
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo> listTopInterfaceInvokeInfo(int limit);
}




