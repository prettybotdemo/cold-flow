package com.warm.flow.orm.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.warm.flow.core.FlowFactory;
import com.warm.flow.core.dao.WarmDao;
import com.warm.flow.core.handler.DataFillHandler;
import com.warm.flow.core.orm.agent.WarmQuery;
import com.warm.flow.orm.mapper.WarmMapper;
import com.warm.tools.utils.ObjectUtil;
import com.warm.tools.utils.StringUtils;
import com.warm.tools.utils.page.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * BaseMapper接口
 *
 * @author warm
 * @date 2023-03-17
 */
public abstract class WarmDaoImpl<T> implements WarmDao<T> {

    public abstract WarmMapper<T> getMapper();

    /**
     * 根据id查询
     *
     * @param id 主键
     * @return 实体
     */
    @Override
    public T selectById(Serializable id) {
        return getMapper().selectById(id);
    }


    /**
     * 根据ids查询
     *
     * @param ids 主键
     * @return 实体
     */
    @Override
    public List<T> selectByIds(Collection<? extends Serializable> ids) {
        return getMapper().selectBatchIds(ids);
    }

    @Override
    public Page<T> selectPage(T entity, Page<T> page) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> pagePlus =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNum(), page.getPageSize());

        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderBy(StringUtils.isNotEmpty(page.getOrderBy())
//                , page.getIsAsc().equals(SqlKeyword.ASC.getSqlSegment()), page.getOrderBy());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> tPage
                = getMapper().selectPage(pagePlus, queryWrapper);

        if (ObjectUtil.isNotNull(tPage)) {
            return new Page<>(tPage.getRecords(), tPage.getTotal());
        }
        return Page.empty();
    }

    @Override
    public List<T> selectList(T entity, WarmQuery<T> query) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>(entity);
        if (ObjectUtil.isNotNull(query)) {
            queryWrapper.orderBy(StringUtils.isNotEmpty(query.getOrderBy())
                    , query.getIsAsc().equals(SqlKeyword.ASC.getSqlSegment()), query.getOrderBy());
        }
        return getMapper().selectList(queryWrapper);
    }

    @Override
    public long selectCount(T entity) {
        return getMapper().selectCount(new QueryWrapper<>(entity));
    }

    @Override
    public int save(T entity) {
        insertFill(entity);
        return insert(entity);
    }

    public int insert(T entity) {
        return getMapper().insert(entity);
    }

    @Override
    public int modifyById(T entity) {
        updateFill(entity);
        return updateById(entity);
    }

    public int updateById(T entity) {
        return getMapper().updateById(entity);
    }

    @Override
    public int delete(T entity) {
        return getMapper().deleteById(entity);
    }

    @Override
    public int deleteById(Serializable id) {
        return getMapper().deleteById(id);
    }

    @Override
    public int deleteByIds(Collection<? extends Serializable> ids) {
        return getMapper().deleteBatchIds(ids);
    }

    public void insertFill(T entity) {
        DataFillHandler dataFillHandler = FlowFactory.dataFillHandler();
        if (ObjectUtil.isNotNull(dataFillHandler)) {
            dataFillHandler.idFill(entity);
            dataFillHandler.insertFill(entity);
        }
    }

    public void updateFill(T entity) {
        DataFillHandler dataFillHandler = FlowFactory.dataFillHandler();
        if (ObjectUtil.isNotNull(dataFillHandler)) {
            dataFillHandler.updateFill(entity);
        }
    }
}
