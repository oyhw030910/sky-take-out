package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface MealService {
    void save(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO getById(Long id);

    void changeMeal(SetmealDTO setmealDTO);

    void changeStatus(int status, Long id);

    void deleteBatch(List<Long> ids);


    List<DishItemVO> getDishViewByMealId(Long id);

    List<Setmeal> list(Setmeal setmeal);
}
