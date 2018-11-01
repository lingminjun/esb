package com.venus.gen.dao;

import java.io.Serializable;
import java.util.List;

/**
 * User: 凌敏均
 * Date: 17/12/06
 * Description: 用于处理有物理存储表的Dao
 */
public interface TableDAO<DO extends Serializable> extends ViewDAO<DO> {

    /**
     * 添加实体
     *
     * @param entity 要添加的实体对象
     * @return
     */
    public long insert(DO entity);


    /**
     * 新建对象或更新对象
     *
     * @param entity 要保存的实体对象
     * @return
     */
    public long insertOrUpdate(DO entity);

    /**
     * 批量添加实体
     *
     * @param entities 要添加的实体对象
     * @return
     */
    public long batchInsert(List<DO> entities);

    /**
     * 保存实体
     *
     * @param entity 要保存的实体对象
     * @return
     */
    public long update(DO entity);


    /**
     * 根据主键删除实体
     *
     * @param pk 主键
     * @return
     */
    public long deleteById(Long pk);

    /**
     * 根据主键返回指定的实体对象
     *
     * @param pk 主键
     * @return
     */
    public DO getById(Long pk);

    /**
     * 根据主键返回指定的实体对象, 会锁表
     *
     * @param pk
     * @return
     */
    public DO getByIdForUpdate(Long pk);

    /**
     * 根据主键集返回指定的实体对象集
     * @param pks
     * @return
     */
    public List<DO> queryByIds(List<Long> pks);

}
