package com.netease.bdms.ndi.service.web.service.impl;

import com.netease.bdms.ndi.service.web.dao.ProjectConfigDOMapper;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.pojo.ProjectConfigDO;
import com.netease.bdms.ndi.service.web.service.ProjectConfigService;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @ClassName ProjectConfigServiceImpl
 * @Description Config information of project
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class ProjectConfigServiceImpl implements ProjectConfigService {

    @Autowired
    private ProjectConfigDOMapper configDOMapper;

    @Override
    public String getConfig(String key) {
        return getConfig(key, CommonConstants.NDI_SERVICE_NAME);
    }

    @Override
    public String getConfig(String key, String namespace) {
        return configDOMapper.selectValueByKeyAndNamespace(key.trim(), namespace);
    }

    @Override
    public void setConfig(String key, String value) {
        setConfig(key, value, CommonConstants.NDI_SERVICE_NAME);
    }

    @Override
    public void setConfig(String key, String value, String namespace) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value) || StringUtils.isBlank(namespace)){
            throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Key, value and namespace can't be null");
        }
        ProjectConfigDO projectConfigDO = new ProjectConfigDO();
        projectConfigDO.setConfigKey(key.trim());
        projectConfigDO.setConfigValue(value.trim());
        projectConfigDO.setNamespace(namespace.trim());
        projectConfigDO.setCreateTime(new Date());
        try {
            configDOMapper.insert(projectConfigDO);
        } catch (Exception e) {
            throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Duplicate key and namespace, please modify");
        }
    }

    @Override
    public void updateConfig(String key, String value, String namespace) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value) || StringUtils.isBlank(namespace)){
            throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Key, value and namespace can't be null");
        }
        configDOMapper.updateValueByKeyAndNamespace(key, value, namespace);
    }
}
