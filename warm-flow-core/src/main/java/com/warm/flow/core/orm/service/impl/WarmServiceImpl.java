package com.warm.flow.core.orm.service.impl;


import com.warm.flow.core.dao.WarmDao;
import com.warm.flow.core.orm.agent.WarmQuery;
import com.warm.flow.core.orm.service.IWarmService;
import com.warm.flow.core.utils.SqlHelper;
import com.warm.tools.utils.CollUtil;
import com.warm.tools.utils.page.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * BaseService层处理
 *
 * @author warm
 * @date 2023-03-17
 */
public abstract class WarmServiceImpl<M extends WarmDao<T>, T> implements IWarmService<T> {

    protected M warmDao;

    public M getDao() {
        return warmDao;
    }

    protected abstract IWarmService<T> setDao(M warmDao);

    @Override
    public T getById(Serializable id) {
        return getDao().selectById(id);
    }

    @Override
    public List<T> getByIds(Collection<? extends Serializable> ids) {
        return getDao().selectByIds(ids);
    }

    @Override
    public Page<T> page(T entity, Page<T> page) {
        return getDao().selectPage(entity, page);
    }

    @Override
    public List<T> list(T entity) {
        return getDao().selectList(entity, null);
    }

    @Override
    public List<T> list(T entity, WarmQuery<T> query) {
        return getDao().selectList(entity, query);
    }

    @Override
    public T getOne(T entity) {
        List<T> list = getDao().selectList(entity, null);
        return CollUtil.getOne(list);
    }

    @Override
    public long selectCount(T entity) {
        return getDao().selectCount(entity);
    }

    @Override
    public boolean save(T entity) {
        return SqlHelper.retBool(getDao().save(entity));
    }

    @Override
    public boolean updateById(T entity) {
        return SqlHelper.retBool(getDao().modifyById(entity));
    }

    @Override
    public boolean removeById(Serializable id) {
        return SqlHelper.retBool(getDao().deleteById(id));
    }

    @Override
    public boolean remove(T entity) {
        return SqlHelper.retBool(getDao().delete(entity));
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> ids) {
        return SqlHelper.retBool(getDao().deleteByIds(ids));
    }

    @Override
    public void saveBatch(final List<T> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (T record : list) {
            getDao().save(record);
        }
    }

    @Override
    public void updateBatch(final List<T> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (T record : list) {
            getDao().modifyById(record);
        }
    }

    @Override
    public WarmQuery<T> orderById() {
        return new WarmQuery<>(this).orderById();
    }

    @Override
    public WarmQuery<T> orderByCreateTime() {
        return new WarmQuery<>(this).orderByCreateTime();
    }

    @Override
    public WarmQuery<T> orderByUpdateTime() {
        return new WarmQuery<>(this).orderByUpdateTime();
    }

    @Override
    public WarmQuery<T> orderByAsc(String orderByField) {
        return new WarmQuery<>(this).orderByAsc(orderByField);
    }

    @Override
    public WarmQuery<T> orderByDesc(String orderByField) {
        return new WarmQuery<>(this).orderByDesc(orderByField);
    }

    @Override
    public WarmQuery<T> orderBy(String orderByField) {
        return new WarmQuery<>(this).orderBy(orderByField);
    }
}