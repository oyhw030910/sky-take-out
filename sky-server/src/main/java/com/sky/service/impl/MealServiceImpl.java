package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.MealDishMapper;
import com.sky.mapper.MealMapper;
import com.sky.result.PageResult;
import com.sky.service.MealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class MealServiceImpl implements MealService {
    @Autowired
    MealMapper mealMapper;
    @Autowired
    MealDishMapper mealDishMapper;

    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        mealMapper.insert(setmeal);
        Long id=setmeal.getId();
        List<SetmealDish> mealDishes=setmealDTO.getSetmealDishes();
        mealDishes.forEach(setmealDish -> setmealDish.setSetmealId(id));
        mealDishMapper.insertBatch(mealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealPageQueryDTO,setmeal);
        Page<Setmeal> page=mealMapper.list(setmeal);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(mealMapper.getById(id),setmealVO);
        List<SetmealDish> dishes=mealDishMapper.getByMealId(id);
        setmealVO.setSetmealDishes(dishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void changeMeal(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        mealMapper.update(setmeal);
        mealDishMapper.deleteByMealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        if(setmealDishes!=null&&!setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDTO.getId()));
        }
        mealDishMapper.insertBatch(setmealDishes);
    }


    @Override
    public void changeStatus(int status, Long id) {
        Setmeal setmeal=Setmeal.builder().id(id).status(status).build();
        mealMapper.update(setmeal);
    }


}
