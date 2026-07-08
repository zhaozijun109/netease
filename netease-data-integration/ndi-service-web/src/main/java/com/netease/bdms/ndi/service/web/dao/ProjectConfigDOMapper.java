package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ProjectConfigDO;
import org.apache.ibatis.annotations.Param;

public interface ProjectConfigDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ProjectConfigDO record);

    int insertSelective(ProjectConfigDO record);

    ProjectConfigDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProjectConfigDO record);

    int updateByPrimaryKey(ProjectConfigDO record);

    String selectValueByKey(String key);

    String selectValueByKeyAndNamespace(@Param(value = "key") String key,
                                        @Param(value = "namespace") String namespace);

    int updateValueByKeyAndNamespace(@Param(value = "key") String key,
                                     @Param(value = "value") String value,
                                     @Param(value = "namespace") String namespace);
}