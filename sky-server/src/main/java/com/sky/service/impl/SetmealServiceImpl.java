package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐,同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithDish(SetmealDTO setmealDTO) {
        //插入套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.insert(setmeal);

        //插入菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //获取套餐id
        Long setmealId = setmeal.getId();

        if (setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        //根据套餐名称(unique)查询套餐基本信息
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        //判断当前套餐启售状态
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //批量删除套餐
        setmealMapper.deleteByIds(ids);

        //批量删除套餐关联菜品信息
        setmealDishMapper.deleteBySetmealIds(ids);

    }

    /**
     * 根据套餐id查询套餐基本信息和关联菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //查询套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);

        //查询套餐关联菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        //封装到setmealVO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {
        //修改setmeal基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.update(setmeal);

        //删除当前setmeal关联菜品信息
        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        //添加setmeal关联菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 7套餐起售停售
     * @param status
     * @param id
     */
    @Override
    public void StartOrStop(Integer status, Long id) {
        //起售套餐时,判断套餐是否有停售关联菜品,有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(StatusConstant.ENABLE.equals(status)){
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && !dishList.isEmpty()){
                for (Dish dish : dishList) {
                    if (StatusConstant.DISABLE.equals(dish.getStatus())){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
