package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 插入套餐关联菜品信息
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id批量删除套餐关联菜品信息
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 根据套餐id查询关联菜品信息
     * @param SetmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{SetmealId}")
    List<SetmealDish> getBySetmealId(Long SetmealId);

    /**
     * 根据套餐id删除关联菜品信息
     * @param SetmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{SetmealId}")
    void deleteBySetmealId(Long SetmealId);
}
