package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MealDishMapper {
    void insertBatch(List<SetmealDish> mealDishes);


    List<SetmealDish> getByMealId(Long id);

    @Delete("delete from setmeal_dish where setmeal_id=#{id}")
    void deleteByMealId(Long id);

    void deleteBatch(List<Long> ids);
}
