package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MealMapper {
    List<Long> getMealIdByDishIds(List<Long> ids);
}
