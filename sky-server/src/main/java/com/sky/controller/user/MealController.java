package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.MealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("UserMealController")
@RequestMapping("/user/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class MealController {

    @Autowired
    private MealService mealService;



    @GetMapping("list")
    @ApiOperation("套餐相关接口")
    @Cacheable(cacheNames = "mealCache",key = "#categoryId")
    public Result<List<Setmeal>> list(Long categoryId){
        log.info("根据分类id查询套餐,{}",categoryId);
        Setmeal setmeal=Setmeal.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        List<Setmeal> setmeals=mealService.list(setmeal);
        return Result.success(setmeals);
    }
    @GetMapping("/dish/{id}")
    @ApiOperation("查询套餐下菜品")
    @Cacheable(cacheNames = "mealDish",key = "#id")
    public Result<List<DishItemVO>> getDishes(@PathVariable Long id){
        log.info("查询套餐下菜品,{}",id);
        List<DishItemVO> dishItemVOS=mealService.getDishViewByMealId(id);
        return Result.success(dishItemVOS);
    }
}
