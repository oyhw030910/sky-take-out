package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.MealDishMapper;
import com.sky.mapper.MealMapper;
import com.sky.result.PageResult;
import com.sky.service.MealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MealServiceImpl implements MealService {
    @Autowired
    MealMapper mealMapper;
    @Autowired
    MealDishMapper mealDishMapper;

    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        mealMapper.insert(setmeal);
        Long id=setmeal.getId();
        List<SetmealDish> mealDishes=setmealDTO.getSetmealDishes();
        mealDishes.forEach(setmealDish -> setmealDish.setDishId(id));
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
}
