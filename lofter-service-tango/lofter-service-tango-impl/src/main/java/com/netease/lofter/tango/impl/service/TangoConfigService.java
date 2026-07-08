package com.netease.lofter.tango.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Sets;
import com.netease.lofter.acl.sdk.context.UserInfoHolder;
import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.api.consts.TangoConfigOpType;
import com.netease.lofter.tango.api.dto.config.TangoConfigDTO;
import com.netease.lofter.tango.impl.delegate.TangoConfigDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.TangoConfigDO;
import com.netease.lofter.tango.impl.helper.ApolloHelper;
import com.netease.lofter.tango.impl.helper.ProfileEnv;
import com.netease.lofter.tango.impl.helper.TangoConfigHelper;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.web.query.TangoConfigQuery;
import com.netease.lofter.tango.impl.web.ro.ConfigGetRO;
import com.netease.lofter.tango.impl.web.ro.ConfigGrantRO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PageVO;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.web.vo.TangoConfigOpHistoryVO;
import com.netease.lofter.tango.impl.web.vo.config.TangoConfigUpdateVO;
import com.netease.lofter.tango.impl.web.vo.config.TangoConfigVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.mm.tk.common.util.lang.CollectionUtils3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class TangoConfigService {

    @Autowired
    private TangoConfigDelegate tangoConfigDelegate;
    @Autowired
    private TangoConfigOpHistoryService tangoConfigOpHistoryService;
    @Autowired
    private TangoAccessUsersService tangoAccessUsersService;
    @Autowired
    private TangoUserService tangoUserService;
    @Autowired
    private ApolloHelper apolloHelper;
    @Autowired
    private ProfileEnv profileEnv;
    @Autowired
    private TangoConfigHelper tangoConfigHelper;

    @Value("${gly.tango.test:''}")
    public void setConfigChange(String value) {
        log.info("gly.tango.test: {}", value);
    }

    public Boolean add(TangoConfigVO configVO) {
        TangoConfigDO tangoConfigDO = tangoConfigDelegate.selectByAppIdAndKey(configVO.getAppId(), configVO.getConfigKey(), profileEnv.getEnv());
        AssertUtils.isTrue(tangoConfigDO == null, "配置已存在");
        TangoConfigDO tangoAppDO = BeanConvertUtils.convertBean(configVO, TangoConfigDO.class);
        tangoAppDO.setOperator(configVO.getOperator());
        tangoAppDO.setCreateTime(System.currentTimeMillis());
        tangoAppDO.setUpdateTime(tangoAppDO.getCreateTime());
        tangoAppDO.setCreator(configVO.getOperator());
        tangoAppDO.setEnvTags(profileEnv.env());
        tangoConfigDelegate.insertValue(tangoAppDO);
        this.addOpSnapshot(TangoConfigOpType.ADD, configVO.getOperator(), tangoAppDO, null);
        return true;
    }

    public Boolean syncOnline(PrimiaryKey pk, String operator) {
        TangoConfigDO preConfig = tangoConfigDelegate.selectById(pk.getId());
        AssertUtils.notNull(preConfig, "配置不存在");
        AssertUtils.isTrue(profileEnv.isPre(preConfig.getEnvTags()) , "只能同步预发环境配置");
        TangoConfigDO onlineConfig = tangoConfigDelegate.selectByAppIdAndKey(preConfig.getAppId(), preConfig.getConfigKey(), ProfileEnv.ENV_PRO);
        AssertUtils.isTrue(onlineConfig == null, "线上配置已存在");
        preConfig.setCreateTime(System.currentTimeMillis());
        preConfig.setUpdateTime(System.currentTimeMillis());
        preConfig.setOperator(operator);
        preConfig.setCreator(operator);
        preConfig.setEnvTags(ProfileEnv.ENV_PRO);
        preConfig.setId(null);
        tangoConfigDelegate.insertValue(preConfig);
        return true;
    }


    public Boolean delete(Long id, String operator) {
        if (null == id) {
            return false;
        }
        TangoConfigDO tangoConfigDO = tangoConfigDelegate.selectById(id);
        AssertUtils.notNull(tangoConfigDO, "配置不存在");
        AssertUtils.isTrue(canEdit(populateAllowUser(tangoConfigDO.getUserlist()), tangoConfigDO.getCreator()), "无操作权限");
        int cnt = tangoConfigDelegate.deleteById(id);
        if (cnt == 0) {
            return false;
        }
        this.addOpSnapshot(TangoConfigOpType.DELETE, operator, null, tangoConfigDO);
        tangoConfigHelper.invalidateCache(tangoConfigDO.getAppId(), tangoConfigDO.getConfigKey());
        return true;
    }

    public Boolean update(TangoConfigUpdateVO tangoConfigUpdateVO) {
        AssertUtils.notNull(tangoConfigUpdateVO.getId(), "配置id不能为空");
        TangoConfigDO old = tangoConfigDelegate.selectById(tangoConfigUpdateVO.getId());
        AssertUtils.notNull(old, "配置不存在");
        AssertUtils.isTrue(canEdit(populateAllowUser(old.getUserlist()), old.getCreator()), "无操作权限");
        TangoConfigDO tangoConfigDO = BeanConvertUtils.convertBean(old, TangoConfigDO.class);
        tangoConfigDO.setId(tangoConfigUpdateVO.getId());
        tangoConfigDO.setConfigValue(tangoConfigUpdateVO.getConfigValue());
        tangoConfigDO.setDescription(tangoConfigUpdateVO.getDescription());
        tangoConfigDO.setOperator(tangoConfigUpdateVO.getOperator());
        tangoConfigDO.setConfigMeta(tangoConfigUpdateVO.getConfigMeta());
        tangoConfigDO.setUpdateTime(System.currentTimeMillis());
        tangoConfigDO.setEnvTags(old.getEnvTags());
        tangoConfigDelegate.updateValue(tangoConfigDO);
        this.addOpSnapshot(TangoConfigOpType.UPDATE, tangoConfigUpdateVO.getOperator(), tangoConfigDO, old);
        tangoConfigHelper.invalidateCache(tangoConfigDO.getAppId(), tangoConfigDO.getConfigKey());
        return true;
    }

    public Boolean grant(ConfigGrantRO configGrantRO) {
        AssertUtils.notNull(configGrantRO.getConfigId(), "配置id不能为空");
        TangoConfigDO old = tangoConfigDelegate.selectById(configGrantRO.getConfigId());
        AssertUtils.notNull(old, "配置不存在");
        AssertUtils.isTrue(granted(old.getCreator()), "无操作权限");
        old.setUserlist(JSON.toJSONString(Optional.ofNullable(configGrantRO.getEmails()).orElse(Collections.emptyList())));
        tangoConfigDelegate.updateValue(old);
        return true;
    }


    public PageResult<TangoConfigVO> listByQuery(TangoConfigQuery tangoConfigQuery) {
        PageResult<TangoConfigVO> pageResult = new PageResult<>(tangoConfigQuery.getPage());
        PageDO<TangoConfigDO> pageDO = tangoConfigDelegate.listByQuery(tangoConfigQuery.getAppId(), tangoConfigQuery.getConfigKey(), profileEnv.getEnv(),
                tangoConfigQuery.getOffset(), tangoConfigQuery.getLimit());
        List<String> keys = CollectionUtils3.distinct(pageDO.getList(), TangoConfigDO::getConfigKey);
        Set<String> onlineKeys = getOnlineKeys(tangoConfigQuery.getAppId(), keys);
        return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList(), onlineKeys));
    }

    public TangoConfigVO getByConfigKey(String appId, String configKey) {
        TangoConfigQuery tangoConfigQuery = new TangoConfigQuery();
        tangoConfigQuery.setAppId(appId);
        tangoConfigQuery.setConfigKey(configKey);
        PageResult<TangoConfigVO> page = listByQuery(tangoConfigQuery);
        return CollectionUtils3.firstEle(page.getRecords());
    }

    private Set<String> getOnlineKeys(String appId, List<String> keys) {
        if (!profileEnv.isPre()) {
            return Sets.newHashSet(keys);
        }
        List<TangoConfigDO> onlineConfigs = tangoConfigDelegate.listByAppIdAndKey(appId, ProfileEnv.ENV_PRO, keys);
        return CollectionUtils3.toSet(onlineConfigs, TangoConfigDO::getConfigKey);
    }

    private List<TangoConfigVO> populate2VOList(List<TangoConfigDO> list, Set<String> hasSyncKeys) {
        return BeanConvertUtils.convertList(list, tangoConfigDO -> {
            TangoConfigVO vo = BeanConvertUtils.convertBean(tangoConfigDO, TangoConfigVO.class);
            vo.setAllowUsers(populateAllowUser(tangoConfigDO.getUserlist()));
            vo.setCanGrant(this.granted(tangoConfigDO.getCreator()));
            vo.setCanEdit(this.canEdit(vo.getAllowUsers(), tangoConfigDO.getCreator()));
            vo.setCanSyncOnline(!hasSyncKeys.contains(tangoConfigDO.getConfigKey()));
            return vo;
        });
    }

    private void addOpSnapshot(TangoConfigOpType opType, String operator, TangoConfigDO newValue, TangoConfigDO oldValue) {
        String newValueStr = Optional.ofNullable(newValue).map(TangoConfigDO::getConfigValue).orElse("");
        String oldValueStr = Optional.ofNullable(oldValue).map(TangoConfigDO::getConfigValue).orElse("");
        String newCommentStr = Optional.ofNullable(newValue).map(TangoConfigDO::getDescription).orElse("");
        String oldCommentStr = Optional.ofNullable(oldValue).map(TangoConfigDO::getDescription).orElse("");
        if (Objects.equals(newValueStr, oldValueStr) && Objects.equals(newCommentStr, oldCommentStr)) {
            return;
        }
        TangoConfigDO notNull = Optional.ofNullable(newValue).orElse(oldValue);
        TangoConfigOpHistoryVO opHistoryVO = new TangoConfigOpHistoryVO();
        opHistoryVO.setOperator(operator);
        opHistoryVO.setOpType(opType.DESC);
        opHistoryVO.setConfigKey(notNull.getConfigKey());
        opHistoryVO.setNewValue(newValueStr);
        opHistoryVO.setOldValue(oldValueStr);
        opHistoryVO.setAppId(notNull.getAppId());
        opHistoryVO.setEnvTags(profileEnv.env());
        tangoConfigOpHistoryService.add(opHistoryVO);

        if (Objects.equals(newValueStr, oldValueStr)) {
            return;
        }
        if (tangoConfigHelper.isFrontend(notNull.getAppId())) {
            return;
        }
        apolloHelper.publish(opHistoryVO, opType);
    }

    private Set<String> populateAllowUser(String userlist) {
        if (StringUtils.isBlank(userlist)) {
            return Sets.newHashSet();
        }
        return Sets.newHashSet(JSONArray.parseArray(userlist, String.class));
    }

    private boolean canEdit(Set<String> allowUser, String creator) {
        if (granted(creator)) {
            return true;
        }
        if (CollectionUtils3.isEmpty(allowUser)) {
            return false;
        }
        UserInfo userInfo = UserInfoHolder.getUserInfo();
        return allowUser.contains(userInfo.getEmail());
    }

    private boolean granted(String creator) {
        if (tangoUserService.isAdmin()) {
            return true;
        }
        UserInfo userInfo = UserInfoHolder.getUserInfo();
        if (userInfo != null && userInfo.getEmail().equals(creator)) {
            return true;
        }
        return false;
    }


    public Map<String, String> listByAppIdPublic(String referer, ConfigGetRO configGetRO) {
        if (!tangoConfigHelper.isFrontend(configGetRO.getAppId())) {
            return Collections.emptyMap();
        }
        if (!configGetRO.verifySign(referer)) {
            return Collections.emptyMap();
        }
        Map<String, TangoConfigDTO> map = tangoConfigHelper.listByAppIdAndKey(configGetRO.getAppId(), configGetRO.getKeys());
        return CollectionUtils3.toMap(map.values(), TangoConfigDTO::getConfigKey, TangoConfigDTO::getConfigValue);
    }


}
