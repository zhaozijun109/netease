package com.netease.lofter.tango.impl.service;

import com.netease.lofter.tango.impl.delegate.TangoAccessUsersDelegate;
import com.netease.lofter.tango.impl.entity.TangoAccessUsers;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;
import com.netease.lofter.tango.impl.web.vo.TangoAccessUsersVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.mm.tk.common.util.lang.CollectionUtils3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TangoAccessUsersService {

    @Autowired
    private TangoAccessUsersDelegate tangoAccessUsersDelegate;

    public List<String> listAll() {
        return CollectionUtils3.mapper(tangoAccessUsersDelegate.listAll(), TangoAccessUsers::getEmail);
    }


    public boolean add(TangoAccessUsersVO tangoAccessUsersVO) {
        TangoAccessUsers tangoAccessUsers = BeanConvertUtils.convertBean(tangoAccessUsersVO, TangoAccessUsers.class);
        tangoAccessUsers.setCreateTime(System.currentTimeMillis());
        tangoAccessUsers.setUpdateTime(tangoAccessUsers.getCreateTime());
        tangoAccessUsersDelegate.insertValue(tangoAccessUsers);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tangoAccessUsersDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TangoAccessUsersVO tangoAccessUsersVO) {
        AssertUtils.isTrue(tangoAccessUsersVO != null && tangoAccessUsersVO.getId() != null, "id missing");
        TangoAccessUsers tangoAccessUsers = tangoAccessUsersDelegate.selectById(tangoAccessUsersVO.getId());
        AssertUtils.notNull(tangoAccessUsers, "id illegal");
        BeanUtils.copyNonNullProperties(tangoAccessUsersVO, tangoAccessUsers);
        tangoAccessUsers.setUpdateTime(System.currentTimeMillis());
        tangoAccessUsersDelegate.updateValue(tangoAccessUsers);
        return true;
    }

    private List<TangoAccessUsersVO> populate2VOList(List<TangoAccessUsers> list) {
        return BeanConvertUtils.convertList(list, TangoAccessUsersVO.class);
    }


}
