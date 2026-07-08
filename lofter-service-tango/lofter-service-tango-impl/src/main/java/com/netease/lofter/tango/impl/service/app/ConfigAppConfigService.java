
package com.netease.lofter.tango.impl.service.app;

import com.netease.lofter.tango.impl.delegate.app.ConfigAppConfigMapperDelegate;
import com.netease.lofter.tango.impl.entity.app.ConfigAppConfig;

import com.netease.lofter.tango.impl.web.query.app.ConfigAppConfigQuery;
import com.netease.lofter.tango.impl.web.vo.app.ConfigAppConfigVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ConfigAppConfigService {

    @Autowired
    private ConfigAppConfigMapperDelegate configAppConfigDelegate;

    public PageResult<ConfigAppConfigVO> listByQuery(ConfigAppConfigQuery query) {
        PageResult<ConfigAppConfigVO> pageResult = new PageResult<>(query.getPage());
        PageDO<ConfigAppConfig> pageDO = configAppConfigDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getDescription(),
            query.getPlatform(),
            query.getStartVersion(),
            query.getEndVersion(),
            query.getConfigKey(),
            query.getConfigValue(),
            query.getStatus(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(ConfigAppConfigVO configAppConfigVO) {
        ConfigAppConfig configAppConfig = BeanConvertUtils.convertBean(configAppConfigVO, ConfigAppConfig.class);
        configAppConfig.setCreateTime(System.currentTimeMillis());
        configAppConfig.setStatus(-1);
        if(configAppConfig.getStartVersion() == null) {
            configAppConfig.setStartVersion("");
        }
        if(configAppConfig.getEndVersion() == null) {
            configAppConfig.setEndVersion("");
        }
        configAppConfigDelegate.insertValue(configAppConfig);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = configAppConfigDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(ConfigAppConfigVO configAppConfigVO) {
        AssertUtils.isTrue(configAppConfigVO != null && configAppConfigVO.getId() != null, "id missing");
        ConfigAppConfig configAppConfig = configAppConfigDelegate.selectById(configAppConfigVO.getId());
        AssertUtils.notNull(configAppConfig, "id illegal");
        BeanUtils.copyNonNullProperties(configAppConfigVO, configAppConfig);
        if(configAppConfig.getStartVersion() == null) {
            configAppConfig.setStartVersion("");
        }
        if(configAppConfig.getEndVersion() == null) {
            configAppConfig.setEndVersion("");
        }
        configAppConfigDelegate.updateValue(configAppConfig);
        return true;
    }

    public boolean updateStatus(Long id, Integer status) {
        AssertUtils.isTrue(id != null && status != null, "id or status missing");
        ConfigAppConfig configAppConfig = configAppConfigDelegate.selectById(id);
        AssertUtils.notNull(configAppConfig, "id illegal");

        SqlUpdate update = new SqlUpdate().setLiteral("status", status).whenEquals("id", id).andEquals("status", configAppConfig.getStatus());
        return configAppConfigDelegate.update(update, null) == 1;
    }

    private List<ConfigAppConfigVO> populate2VOList(List<ConfigAppConfig> list) {
        return BeanConvertUtils.convertList(list, ConfigAppConfigVO.class);
    }

}
