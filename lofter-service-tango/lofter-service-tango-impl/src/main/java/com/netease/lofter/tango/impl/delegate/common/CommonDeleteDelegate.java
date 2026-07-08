package com.netease.lofter.tango.impl.delegate.common;

import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.InsertCommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.SelectCommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.UpdateCommonMapper;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.service.InsertCommonService;
import com.netease.yaolu.commons.spring.mybatis.service.SelectCommonService;
import com.netease.yaolu.commons.spring.mybatis.service.UpdateCommonService;

public interface CommonDeleteDelegate<T extends InsertCommonMapper<E> & SelectCommonMapper<E> & UpdateCommonMapper<E> & DeleteCommonMapper<E>, E> extends InsertCommonService<T, E>, SelectCommonService<T, E>, UpdateCommonService<T, E>, DeleteCommonService<T, E> {
}
