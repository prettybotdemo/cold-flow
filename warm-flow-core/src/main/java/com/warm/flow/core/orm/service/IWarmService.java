package com.warm.flow.core.orm.service;


import com.warm.flow.core.orm.agent.WarmQuery;
import com.warm.tools.utils.page.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Service接口
 *
 * @author warm
 * @date 2023-03-17
 */
public interface IWarmService<T> {

    /**
     * 根据id查询
     *
     * @param id 主键
     * @return 实体
     */
    T getById(Serializable id);

    /**
     * 根据ids查询
     *
     * @param ids 主键
     * @return 实体
     */
    List<T> getByIds(Collection<? extends Serializable> ids);

    /**
     * 分页查询并且排序
     *
     * @param entity 实体列表
     * @return 集合
     */
    Page<T> page(T entity, Page<T> page, String order);

    /**
     * 查询列表
     *
     * @param entity 实体列表
     * @return 集合
     */
    List<T> list(T entity);

    /**
     * 查询列表并排序
     *
     * @param entity 实体列表
     * @return 集合
     */
    List<T> list(T entity, String order);

    /**
     * 查询一条记录
     *
     * @param entity 实体列表
     * @return 结果
     */
    T getOne(T entity);

    /**
     * 新增
     *
     * @param entity 实体
     * @return 结果
     */
    boolean save(T entity);

    /**
     * 根据id修改
     *
     * @param entity 实体
     * @return 结果
     */
    boolean updateById(T entity);

    /**
     * 根据id删除
     *
     * @param id 主键
     * @return 结果
     */
    boolean removeById(Serializable id);

    /**
     * 根据entity删除
     *
     * @param entity 实体
     * @return 结果
     */
    boolean remove(T entity);

    /**
     * 根据ids批量删除
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    boolean removeByIds(Collection<? extends Serializable> ids);

    /**
     * 批量新增
     *
     * @param list
     */
    void saveBatch(List<T> list);

    /**
     * 批量更新
     *
     * @param list
     */
    void updateBatch(List<T> list);

    /**
     * 创建时间设置正序排列
     *
     * @return 集合
     */
    WarmQuery<T> orderByCreateTime();

    /**
     * 更新时间设置正序排列
     *
     * @return 集合
     */
    WarmQuery<T> orderByUpdateTime();

    /**
     * 设置正序排列
     *
     * @param orderByField 排序字段
     * @return 集合
     */
    WarmQuery<T> orderByAsc(String orderByField);

    /**
     * 设置倒序排列
     *
     * @param orderByField 排序字段
     * @return 集合
     */
    WarmQuery<T> orderByDesc(String orderByField);

    /**
     * 用户自定义排序方案
     *
     * @param orderByField 排序字段
     * @return 集合
     */
    WarmQuery<T> orderBy(String orderByField);
}
